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
import org.apache.commons.collections4.MapUtils;
import org.apache.log4j.Logger;
import org.training.ai.dto.PromptOptions;
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
 * Action to enhance product description using AI
 */
public class EnhanceDescriptionAction implements CockpitAction<ProductModel, Object> {
    private static final Logger LOG = Logger.getLogger(EnhanceDescriptionAction.class);

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
                final String msg = ctx.getLabel("enhanceDescriptionAction.description.empty", new String[]{locale.toLanguageTag()});
                notificationService.notifyUser(
                        notificationService.getWidgetNotificationSource(ctx),
                        TranslationsaiConstants.NOTIFICATION_TYPE,
                        NotificationEvent.Level.WARNING,
                        msg);
                return new ActionResult<>(ActionResult.ERROR);
            }

            // Build prompt options with default values
            final Map<String, String> enhancedByLocale = getEnhancedDescriptions(locale, product);

            if (MapUtils.isEmpty(enhancedByLocale)) {
                notificationService.notifyUser(
                        notificationService.getWidgetNotificationSource(ctx),
                        TranslationsaiConstants.NOTIFICATION_TYPE,
                        NotificationEvent.Level.FAILURE,
                        ctx.getLabel("enhanceDescriptionAction.failure.ai", new String[]{"empty result"}));
                return new ActionResult<>(ActionResult.ERROR);
            }

            // Build editor dialog with editable fields so user can adjust before saving
            final Window window = createWindow(ctx);

            final Vbox root = new Vbox();
            root.setHflex("1");
            root.setSpacing("10px");

            // Original description (read-only)
            final Label originalLabel = new Label("Original (" + locale.toLanguageTag() + "):");
            root.appendChild(originalLabel);
            final Textbox originalDescriptionTextbox = new Textbox(originalDescription);
            originalDescriptionTextbox.setReadonly(true);
            originalDescriptionTextbox.setMultiline(true);
            originalDescriptionTextbox.setHflex("1");
            originalDescriptionTextbox.setRows(4);
//            originalDescriptionTextbox.setStyle("width: 92%;");
            root.appendChild(originalDescriptionTextbox);
            root.appendChild(new Separator());

            // Editors per locale
            final LinkedHashMap<String, Textbox> editors = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : enhancedByLocale.entrySet()) {
                final String lang = e.getKey();
                final String suggestion = e.getValue();

                final Label langLbl = new Label(lang + ":");
                root.appendChild(langLbl);

                final Textbox editor = new Textbox(suggestion);
                editor.setMultiline(true);
                editor.setHflex("1");
                editor.setRows(6);
//                editor.setStyle("width: 92%;");
                editors.put(lang, editor);
                root.appendChild(editor);
                root.appendChild(new Separator());
            }

            // Buttons (footer pinned)
            final Hbox buttons = new Hbox();
            buttons.setHflex("1");
            buttons.setPack("end");
            buttons.setSpacing("8px");
            final Button okBtn = new Button("OK");
            okBtn.setStyle("background-color:#1976d2;color:#fff;border:1px solid #1976d2;");
            final Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("background-color:#1976d2;color:#fff;border:1px solid #1976d2;");
            buttons.appendChild(cancelBtn);
            buttons.appendChild(okBtn);

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

            okBtn.addEventListener(Events.ON_CLICK, event -> {
                handleCancelBtnEvent(ctx, editors, product, window);
            });

            cancelBtn.addEventListener(Events.ON_CLICK, event -> {
                handleOkBtnEvent(ctx, window);
            });

            // Attach the window to current page before showing modal to avoid SuspendNotAllowedException
            final Desktop desktop = Executions.getCurrent() != null ? Executions.getCurrent().getDesktop() : null;
            if (desktop != null && desktop.getFirstPage() != null) {
                window.setPage(desktop.getFirstPage());
                window.doModal();

                // Return immediately; actual save happens in the clickEvent handler
                return new ActionResult<>(ActionResult.SUCCESS);
            } else {
                LOG.error("Cannot open enhancement dialog: no current ZK desktop/page available");
                notificationService.notifyUser(
                        notificationService.getWidgetNotificationSource(ctx),
                        TranslationsaiConstants.NOTIFICATION_TYPE,
                        NotificationEvent.Level.FAILURE,
                        ctx.getLabel("enhanceDescriptionAction.failure.unexpected", new String[]{"no desktop/page"}));
                return new ActionResult<>(ActionResult.ERROR);
            }
        } catch (final AiClientException e) {
            LOG.error("AI enhancement failed", e);
            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.FAILURE,
                    ctx.getLabel("enhanceDescriptionAction.failure.ai", new String[]{e.getMessage()}));
            return new ActionResult<>(ActionResult.ERROR);
        } catch (final Exception e) {
            LOG.error("Unexpected error during AI enhancement", e);
            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.FAILURE,
                    ctx.getLabel("enhanceDescriptionAction.failure.unexpected", new String[]{e.getMessage()}));
            return new ActionResult<>(ActionResult.ERROR);
        }
    }

    private void handleOkBtnEvent(ActionContext<ProductModel> ctx, Window window) {
        window.detach();
        notificationService.notifyUser(
                notificationService.getWidgetNotificationSource(ctx),
                TranslationsaiConstants.NOTIFICATION_TYPE,
                NotificationEvent.Level.INFO,
                ctx.getLabel("enhanceDescriptionAction.cancelled"));
    }

    private void handleCancelBtnEvent(ActionContext<ProductModel> ctx, LinkedHashMap<String, Textbox> editors, ProductModel product, Window window) {
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
                    ctx.getLabel("enhanceDescriptionAction.success"));
            window.detach();
        } catch (Exception ex) {
            LOG.error("Failed to apply enhanced description", ex);
            notificationService.notifyUser(
                    notificationService.getWidgetNotificationSource(ctx),
                    TranslationsaiConstants.NOTIFICATION_TYPE,
                    NotificationEvent.Level.FAILURE,
                    ctx.getLabel("enhanceDescriptionAction.failure.apply", new String[]{ex.getMessage()}));
        }
    }

    private static Borderlayout createBorderLayout() {
        final Borderlayout borderlayout = new Borderlayout();
        borderlayout.setHflex("1");
        borderlayout.setVflex("1");
        return borderlayout;
    }

    private static Window createWindow(ActionContext<ProductModel> ctx) {
        final Window dlg = new Window();
        dlg.setTitle(ctx.getLabel("enhanceDescriptionAction.preview.title"));
        dlg.setWidth("900px");
        dlg.setHeight("70%");
        dlg.setClosable(true);
        dlg.setBorder("normal");
        return dlg;
    }

    private Map<String, String> getEnhancedDescriptions(Locale locale, ProductModel product) {
        final PromptOptions options = new PromptOptions();
        options.setLanguage(locale.toLanguageTag());
        options.setTone("professional");
        options.setPreserveTechnicalTerms(true);

        // Determine target languages: all system locales, base first
        final Set<Locale> allLocales = i18nService.getSupportedLocales();
        final List<String> targetLangs = allLocales.stream()
                .map(Locale::toLanguageTag)
                .sorted()
                .collect(Collectors.toList());
        if (!targetLangs.contains(locale.toLanguageTag())) {
            targetLangs.add(0, locale.toLanguageTag());
        }
        options.setTargetLanguages(targetLangs);

        final Map<String, String> enhancedByLocale = translationsAiService.enhanceDescription(product, locale, options);
        return enhancedByLocale;
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

}
