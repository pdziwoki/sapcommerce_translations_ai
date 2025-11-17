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
import org.apache.log4j.Logger;
import org.training.ai.dto.PromptOptions;
import org.training.ai.exception.AiClientException;
import org.training.ai.service.TranslationsAiService;
import org.training.constants.TranslationsaiConstants;
import org.zkoss.zul.Messagebox;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
            final PromptOptions options = new PromptOptions();
            options.setLanguage(locale.toLanguageTag());
            options.setTone("professional");
            options.setPreserveTechnicalTerms(true);

            // Determine target languages: all system locales, base first
            final Set<Locale> allLocales = i18nService.getSupportedLocales();
            final List<String> targetLangs = allLocales.stream()
                    .map(Locale::toLanguageTag)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            if (!targetLangs.contains(locale.toLanguageTag())) {
                targetLangs.add(0, locale.toLanguageTag());
            }
            options.setTargetLanguages(targetLangs);

            // Call AI service
            final Map<String, String> enhancedByLocale = translationsAiService.enhanceDescription(product, locale, options);

            if (enhancedByLocale == null || enhancedByLocale.isEmpty()) {
                notificationService.notifyUser(
                        notificationService.getWidgetNotificationSource(ctx),
                        TranslationsaiConstants.NOTIFICATION_TYPE,
                        NotificationEvent.Level.FAILURE,
                        ctx.getLabel("enhanceDescriptionAction.failure.ai", new String[]{"empty result"}));
                return new ActionResult<>(ActionResult.ERROR);
            }

            // Build preview string
            final StringBuilder previewBuilder = new StringBuilder();
            previewBuilder.append(ctx.getLabel("enhanceDescriptionAction.preview.title")).append("\n\n");
            previewBuilder.append("Original (" + locale.toLanguageTag() + "):\n").append(originalDescription).append("\n\n---\n\n");
            for (Map.Entry<String, String> e : enhancedByLocale.entrySet()) {
                previewBuilder.append(e.getKey()).append(":\n").append(e.getValue()).append("\n\n");
            }
            previewBuilder.append("Apply these enhanced descriptions?");

            Messagebox.show(
                    previewBuilder.toString(),
                    ctx.getLabel("enhanceDescriptionAction.preview.title"),
                    new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
                    null,
                    null,
                    null,
                    (Messagebox.ClickEvent clickEvent) -> {
                        if (clickEvent != null && clickEvent.getButton() != null && Messagebox.Button.OK.equals(clickEvent.getButton())) {
                            try {
                                // Apply enhanced descriptions to each locale
                                for (Map.Entry<String, String> e : enhancedByLocale.entrySet()) {
                                    final Locale loc = Locale.forLanguageTag(e.getKey());
                                    product.setDescription(e.getValue(), loc);
                                }
                                modelService.save(product);

                                // Refresh the object in UI
                                objectFacade.reload(product);

                                notificationService.notifyUser(
                                        notificationService.getWidgetNotificationSource(ctx),
                                        TranslationsaiConstants.NOTIFICATION_TYPE,
                                        NotificationEvent.Level.SUCCESS,
                                        ctx.getLabel("enhanceDescriptionAction.success"));
                            } catch (Exception ex) {
                                LOG.error("Failed to apply enhanced description", ex);
                                notificationService.notifyUser(
                                        notificationService.getWidgetNotificationSource(ctx),
                                        TranslationsaiConstants.NOTIFICATION_TYPE,
                                        NotificationEvent.Level.FAILURE,
                                        ctx.getLabel("enhanceDescriptionAction.failure.apply", new String[]{ex.getMessage()}));
                            }
                        } else {
                            // Cancel pressed or dialog closed
                            notificationService.notifyUser(
                                    notificationService.getWidgetNotificationSource(ctx),
                                    TranslationsaiConstants.NOTIFICATION_TYPE,
                                    NotificationEvent.Level.INFO,
                                    ctx.getLabel("enhanceDescriptionAction.cancelled"));
                        }
                    }
            );

            // Return immediately; actual save happens in the clickEvent handler
            return new ActionResult<>(ActionResult.SUCCESS);
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
