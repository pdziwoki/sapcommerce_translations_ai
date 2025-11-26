package org.training.backoffice.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEvent;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.training.ai.dto.options.PromptOptions;
import org.training.ai.dto.response.Translation;
import org.training.ai.exception.AiClientException;
import org.training.ai.service.TranslationsAiService;
import org.training.constants.TranslationsaiConstants;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Action to translate and eventually enhance product description using AI
 */
public class TranslateDescriptionAction implements CockpitAction<ProductModel, Object> {
    private static final Logger LOG = Logger.getLogger(TranslateDescriptionAction.class);

    @Resource
    private TranslationsAiService translationsAiService;
    @Resource
    private ModelService modelService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private I18NService i18nService;
    @Resource
    private ObjectFacade objectFacade;

    @Override
    public ActionResult<Object> perform(final ActionContext<ProductModel> ctx) {
        final ProductModel product = ctx.getData();
        if (product == null) {
            return new ActionResult<>(ActionResult.ERROR);
        }

        try {
            // Get current locale (use session locale or default)
            final Locale locale = i18nService.getCurrentLocale();

            // Get current description
            final String originalDescription = product.getDescription(locale);
            if (originalDescription == null || originalDescription.trim().isEmpty()) {
                final String msg = ctx.getLabel("translateDescriptionAction.description.empty", new String[]{locale.toLanguageTag()});
                notificationService.notifyUser(
                        notificationService.getWidgetNotificationSource(ctx),
                        TranslationsaiConstants.NOTIFICATION_TYPE,
                        NotificationEvent.Level.WARNING,
                        msg);
                return new ActionResult<>(ActionResult.ERROR);
            }

            // Show a small options window with a checkbox and OK/CANCEL
            final Window optionsWindow = createOptionsWindow(ctx);

            final Vbox box = new Vbox();
            box.setHflex("1");
            box.setSpacing("10px");

            final Checkbox enhanceCheckbox = new Checkbox("Enhance source description");
            enhanceCheckbox.setChecked(false);
            box.appendChild(enhanceCheckbox);

            final Hbox buttons = new Hbox();
            buttons.setHflex("1");
            buttons.setPack("end");
            buttons.setSpacing("8px");

            final Button cancelBtn = new Button(ctx.getLabel("translateDescriptionAction.cancel.button.label"));
            cancelBtn.setStyle("background-color:#1976d2;color:#fff;border:1px solid #1976d2;");
            final Button translateBtn = new Button(ctx.getLabel("translateDescriptionAction.translate.button.label"));
            translateBtn.setStyle("background-color:#1976d2;color:#fff;border:1px solid #1976d2;");
            buttons.appendChild(cancelBtn);
            buttons.appendChild(translateBtn);

            final Borderlayout borderlayout = createBorderLayout();
            final Center center = new Center();
            final Div contentWrapper = new Div();
            contentWrapper.setHflex("1");
            contentWrapper.setStyle("padding: 12px 24px 0 12px;");
            contentWrapper.appendChild(box);
            center.appendChild(contentWrapper);
            borderlayout.appendChild(center);

            final South south = new South();
            south.setHeight("50px");
            south.appendChild(buttons);
            borderlayout.appendChild(south);

            optionsWindow.appendChild(borderlayout);

            cancelBtn.addEventListener(Events.ON_CLICK, event -> optionsWindow.detach());
            translateBtn.addEventListener(Events.ON_CLICK, event -> {
                try {
                    final boolean enhance = enhanceCheckbox.isChecked();
                    optionsWindow.detach();

                    final List<Translation> translations = getDescriptionsForOption(locale, product, enhance);
                    if (CollectionUtils.isEmpty(translations)) {
                        notificationService.notifyUser(
                                notificationService.getWidgetNotificationSource(ctx),
                                TranslationsaiConstants.NOTIFICATION_TYPE,
                                NotificationEvent.Level.FAILURE,
                                ctx.getLabel("translateDescriptionAction.failure.ai", new String[]{"empty result"}));
                        return;
                    }

                    showResultsWindow(ctx, product, locale, originalDescription, translations);
                } catch (final AiClientException e) {
                    LOG.error("AI translation failed", e);
                    notificationService.notifyUser(
                            notificationService.getWidgetNotificationSource(ctx),
                            TranslationsaiConstants.NOTIFICATION_TYPE,
                            NotificationEvent.Level.FAILURE,
                            ctx.getLabel("translateDescriptionAction.failure.ai", new String[]{e.getMessage()}));
                } catch (final Exception e) {
                    LOG.error("Unexpected error during AI translation", e);
                    notificationService.notifyUser(
                            notificationService.getWidgetNotificationSource(ctx),
                            TranslationsaiConstants.NOTIFICATION_TYPE,
                            NotificationEvent.Level.FAILURE,
                            ctx.getLabel("translateDescriptionAction.failure.unexpected", new String[]{e.getMessage()}));
                }
            });

            // Attach the window to current page before showing modal to avoid SuspendNotAllowedException
            final Desktop desktop = Executions.getCurrent() != null ? Executions.getCurrent().getDesktop() : null;
            if (desktop != null && desktop.getFirstPage() != null) {
                optionsWindow.setPage(desktop.getFirstPage());
                optionsWindow.doModal();
                return new ActionResult<>(ActionResult.SUCCESS);
            } else {
                LOG.error("Cannot open options dialog: no current ZK desktop/page available");
                notificationService.notifyUser(
                        notificationService.getWidgetNotificationSource(ctx),
                        TranslationsaiConstants.NOTIFICATION_TYPE,
                        NotificationEvent.Level.FAILURE,
                        ctx.getLabel("translateDescriptionAction.failure.unexpected", new String[]{"no desktop/page"}));
                return new ActionResult<>(ActionResult.ERROR);
            }
        } catch (final AiClientException e) {
            LOG.error("AI translation failed", e);
            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.FAILURE,
                    ctx.getLabel("translateDescriptionAction.failure.ai", new String[]{e.getMessage()}));
            return new ActionResult<>(ActionResult.ERROR);
        } catch (final Exception e) {
            LOG.error("Unexpected error during AI translation", e);
            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.FAILURE,
                    ctx.getLabel("translateDescriptionAction.failure.unexpected", new String[]{e.getMessage()}));
            return new ActionResult<>(ActionResult.ERROR);
        }
    }

    private void handleCancelBtnEvent(ActionContext<ProductModel> ctx, Window window) {
        window.detach();
        notificationService.notifyUser(
                notificationService.getWidgetNotificationSource(ctx),
                TranslationsaiConstants.NOTIFICATION_TYPE,
                NotificationEvent.Level.INFO,
                ctx.getLabel("translateDescriptionAction.cancelled"));
    }

    private void handleSaveBtnEvent(ActionContext<ProductModel> ctx, LinkedHashMap<String, Textbox> editors, ProductModel product, Window window) {
        try {
            for (Map.Entry<String, Textbox> entry : editors.entrySet()) {
                final Locale loc = Locale.forLanguageTag(entry.getKey());
                final String value = entry.getValue().getValue();
                product.setDescription(value, loc);
            }
            modelService.save(product);
            objectFacade.reload(product);

            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.SUCCESS,
                    ctx.getLabel("translateDescriptionAction.success"));
            window.detach();
        } catch (Exception ex) {
            LOG.error("Failed to apply enhanced description", ex);
            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.FAILURE,
                    ctx.getLabel("translateDescriptionAction.failure.apply", new String[]{ex.getMessage()}));
        }
    }

    private static Borderlayout createBorderLayout() {
        final Borderlayout borderlayout = new Borderlayout();
        borderlayout.setHflex("1");
        borderlayout.setVflex("1");
        return borderlayout;
    }

    private static Window createWindow(ActionContext<ProductModel> ctx) {
        final Window window = new Window();
        window.setTitle(ctx.getLabel("translateDescriptionAction.preview.title"));
        window.setWidth("900px");
        window.setHeight("70%");
        window.setClosable(true);
        window.setBorder("normal");
        return window;
    }

    private List<Translation> getEnhancedDescriptions(final Locale locale, final ProductModel product) {
        final PromptOptions options = new PromptOptions();
        options.setSourceLanguage(locale.toLanguageTag());
        options.setTone("professional");

        // Determine target languages: all system locales, base first
        final Set<Locale> allLocales = i18nService.getSupportedLocales();
        final List<String> targetLangs = allLocales.stream()
                .map(Locale::toLanguageTag)
                .filter(lang -> !lang.equals(locale.toLanguageTag()))
                .collect(Collectors.toList());

        targetLangs.add(0, locale.toLanguageTag());

        options.setTargetLanguages(targetLangs);

        return translationsAiService.translateDescription(product, locale, options);
    }

    @Override
    public boolean canPerform(final ActionContext<ProductModel> ctx) {
        return ctx.getData() != null && translationsAiService.isEnabled();
    }

    @Override
    public boolean needsConfirmation(final ActionContext<ProductModel> ctx) {
        return false;
    }

    @Override
    public String getConfirmationMessage(final ActionContext<ProductModel> ctx) {
        return null;
    }

    private Window createOptionsWindow(ActionContext<ProductModel> ctx) {
        final Window window = new Window();
        window.setTitle("Translations AI");
        window.setWidth("420px");
        window.setHeight("180px");
        window.setClosable(true);
        window.setBorder("normal");
        return window;
    }

    private void showResultsWindow(final ActionContext<ProductModel> ctx,
                                   final ProductModel product,
                                   final Locale locale,
                                   final String originalDescription,
                                   final List<Translation> translations) {
        final Window window = createWindow(ctx);

        final Vbox root = new Vbox();
        root.setHflex("1");
        root.setSpacing("10px");

        final Label originalLabel = new Label("Original (" + locale.toLanguageTag() + "):");
        root.appendChild(originalLabel);
        final Textbox originalDescriptionTextbox = new Textbox(originalDescription);
        originalDescriptionTextbox.setReadonly(true);
        originalDescriptionTextbox.setMultiline(true);
        originalDescriptionTextbox.setHeight("80px");
        originalDescriptionTextbox.setHflex("1");
        root.appendChild(originalDescriptionTextbox);
        root.appendChild(new Separator());

        final LinkedHashMap<String, Textbox> editors = new LinkedHashMap<>();
        for (Translation translation : translations) {
            final String lang = translation.getLang();
            final String suggestion = translation.getDescription();

            final Label langLbl = new Label(lang + ":");
            root.appendChild(langLbl);

            final Textbox editor = new Textbox(suggestion);
            editor.setMultiline(true);
            editor.setHeight("80px");
            editor.setHflex("1");
            editors.put(lang, editor);
            root.appendChild(editor);
            root.appendChild(new Separator());
        }

        final Hbox buttons = new Hbox();
        buttons.setHflex("1");
        buttons.setPack("end");
        buttons.setSpacing("8px");
        final Button saveBtn = new Button(ctx.getLabel("translateDescriptionAction.ok.button.label"));
        saveBtn.setStyle("background-color:#1976d2;color:#fff;border:1px solid #1976d2;");
        final Button cancelBtn = new Button(ctx.getLabel("translateDescriptionAction.cancel.button.label"));
        cancelBtn.setStyle("background-color:#1976d2;color:#fff;border:1px solid #1976d2;");
        buttons.appendChild(cancelBtn);
        buttons.appendChild(saveBtn);

        final Borderlayout borderlayout = createBorderLayout();

        final Center center = new Center();
        center.setAutoscroll(true);
        final Div contentWrapper = new Div();
        contentWrapper.setHflex("1");
        contentWrapper.appendChild(root);
        contentWrapper.setStyle("padding: 0 24px 24px 0;");
        center.appendChild(contentWrapper);
        borderlayout.appendChild(center);

        final South south = new South();
        south.setHeight("60px");
        south.appendChild(buttons);
        borderlayout.appendChild(south);

        window.appendChild(borderlayout);

        saveBtn.addEventListener(Events.ON_CLICK, event -> {
            handleSaveBtnEvent(ctx, editors, product, window);
        });

        cancelBtn.addEventListener(Events.ON_CLICK, event -> {
            handleCancelBtnEvent(ctx, window);
        });

        final Desktop desktop = Executions.getCurrent() != null ? Executions.getCurrent().getDesktop() : null;
        if (desktop != null && desktop.getFirstPage() != null) {
            window.setPage(desktop.getFirstPage());
            window.doModal();
        } else {
            LOG.error("Cannot open translation dialog: no current ZK desktop/page available");
            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.FAILURE,
                    ctx.getLabel("translateDescriptionAction.failure.unexpected", new String[]{"no desktop/page"}));
        }
    }

    private List<Translation> getDescriptionsForOption(final Locale locale,
                                                       final ProductModel product,
                                                       final boolean enhance) {
        final PromptOptions options = new PromptOptions();
        options.setSourceLanguage(locale.toLanguageTag());
        options.setTone("professional");
        options.setEnhanceSource(enhance);

        final Set<Locale> allLocales = i18nService.getSupportedLocales();
        final List<String> targetLangs = allLocales.stream()
                .map(Locale::toLanguageTag)
                .filter(lang -> !lang.equals(locale.toLanguageTag()))
                .collect(Collectors.toList());

        if (enhance) {
            // include base language at first position
            targetLangs.add(0, locale.toLanguageTag());
        }
        options.setTargetLanguages(targetLangs);

        return translationsAiService.translateDescription(product, locale, options);
    }
}
