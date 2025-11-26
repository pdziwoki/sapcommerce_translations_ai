# AI-Powered Product Description Enhancement

## Overview
This extension now includes an AI-powered product description enhancement feature that uses OpenAI's GPT models to improve product descriptions in the SAP Commerce Cloud backoffice.

## Features
- Backoffice Action: "AI Translate Description" button in the Product editor toolbar
- Modal dialog to preview and edit AI suggestions before applying
- Customizable options:
  - Tone selection (e.g., professional, friendly)
  - Max length control (optional)
- Two modes: enhance+translate or translate-only (via PromptOptions.enhanceSource)
- Multi-language support: suggestions for all supported locales (base language first)

## Architecture

### Service Layer
- **TranslationsAiService**: Main service interface for AI enhancement
- **OpenAiClient**: Java SDK client for OpenAI (structured responses)
- **PromptBuilder**: Constructs effective prompts for the AI model
- **DTOs**: PromptOptions and AiClientOptions for configuration

### UI Components
- TranslateDescriptionAction: Backoffice action that appears in the Product editor and opens a modal dialog for AI suggestions
- ZK Controller classes used by legacy samples are present but not required for the AI dialog

### Configuration
All beans are configured in `translationsai-backoffice-spring.xml`:
- openAiClient: OpenAI Java SDK client (API key injected via Spring property)
- translationsAiService: Main AI service
- Label locator bean for backoffice labels

## Configuration

### 1. Enable the Feature
Default configuration is provided in the extension's `project.properties` file. To enable and configure the feature, override these properties in `local.properties` or environment-specific properties:

```properties
# Enable AI enhancement
translationsai.enabled=true

# Optional: return mock translations without calling OpenAI (useful for demos)
translationsai.mock.response=false

# OpenAI API Configuration (API key is injected to the OpenAiClient bean)
translationsai.openai.apiKey=sk-your-actual-api-key-here
translationsai.openai.model=gpt-4o-mini

# AI Client Options
translationsai.timeout.ms=20000
```

**Note**: Default values are stored in `translationsai/project.properties`. Override them in `local.properties` for your environment.

### 2. Restart the Platform
After configuration, restart SAP Commerce to load the new settings.

## Usage

### Using the Backoffice Action

1. Open the Backoffice and navigate to a Product
2. In the Product editor toolbar, click "AI Translate Description"
3. A dialog will show:
   - Current description
   - AI-enhanced version
4. Click "OK" to apply or "Cancel" to discard

### Result Dialog

When you trigger the action, a dialog opens showing:

- Original description for the base language
- One editable textbox per target language with AI suggestions
- Buttons to Apply (save to product) or Cancel

## Implementation Details

### File Structure
```
translationsai/
├── src/org/training/ai/
│   ├── client/
│   │   ├── AiClient.java (interface)
│   │   └── impl/OpenAiClient.java
│   ├── dto/
│   │   ├── AiClientOptions.java
│   │   └── PromptOptions.java
│   ├── exception/
│   │   └── AiClientException.java
│   ├── service/
│   │   ├── TranslationsAiService.java (interface)
│   │   └── impl/DefaultTranslationsAiService.java
│   └── util/
│       └── PromptBuilder.java
├── backoffice/src/org/training/
│   ├── backoffice/actions/
│   │   └── TranslateDescriptionAction.java
│   └── widgets/
│       └── TranslationsaiController.java
├── backoffice/resources/widgets/TranslationsaiWidget/
│   ├── translationsaiwidget.zul
│   └── labels/labels.properties
└── resources/
    ├── translationsai-backoffice-config.xml
    └── translationsai-backoffice-spring.xml
```

### API Used
- **OpenAI Chat Completions API**: `/v1/chat/completions`
- **Model**: `gpt-4o-mini` (configurable)
- **Temperature**: 0.3 (low randomness for consistent, factual output)
- **Max Tokens**: 512 (adjustable based on needs)

### Error Handling
- Feature disabled: Warning notification
- Empty description: Error message
- API errors: Detailed error notification with message
- Timeout: Configurable via `translationsai.timeout.ms`

## Cost Considerations

OpenAI API usage is billed per token. To control costs:

1. **Set appropriate token limits**: `translationsai.maxTokens=512`
2. **Use gpt-4o-mini**: More cost-effective than gpt-4
3. **Monitor usage**: Check OpenAI dashboard regularly
4. **Disable when not needed**: `translationsai.enabled=false`

## Troubleshooting

### Action doesn't appear
- Check `translationsai.enabled=true` in properties
- Verify Spring bean configuration
- Clear browser cache and restart backoffice

### API errors
- Verify API key is correct
- Check network connectivity to OpenAI
- Review logs for detailed error messages
- Ensure sufficient OpenAI credits

### Empty or poor results
- Adjust `temperature` (0.1-0.5 for factual content)
- Increase `maxTokens` if descriptions are cut off
- Try different tone options
- Ensure source description has meaningful content

## Future Enhancements

Potential improvements:
- Support for multiple AI providers (Azure OpenAI, Anthropic, etc.)
- Batch processing for multiple products
- Description quality scoring
- A/B testing for enhanced descriptions
- Custom prompt templates per category
- History tracking of enhancements

## Security Best Practices

1. **Never commit API keys** to version control
2. Use environment variables: `translationsai.openai.apiKey=${env:OPENAI_API_KEY}`
3. Encrypt sensitive properties in production
4. Restrict backoffice user permissions for AI features
5. Implement rate limiting to prevent abuse
6. Log AI usage for audit purposes

## Support

For issues or questions:
- Check application logs in `hybris/log/tomcat/`
- Review OpenAI API status: https://status.openai.com/
- Contact development team

---

**Version**: 1.0  
**Date**: 2025-11-07  
**Extension**: translationsai
