# AI-Powered Product Description Enhancement

## Overview
This extension now includes an AI-powered product description enhancement feature that uses OpenAI's GPT models to improve product descriptions in the SAP Commerce Cloud backoffice.

## Features
- **Backoffice Action**: "AI Enhance Description" button in Product editor toolbar
- **Widget Interface**: Rich UI for configuring enhancement options and previewing results
- **Customizable Options**:
  - Tone selection (Professional, Friendly, Concise, Detailed)
  - Max length control
  - Technical terms preservation
- **Preview & Apply**: Review AI-enhanced descriptions before applying them
- **Multi-language Support**: Works with the current session locale

## Architecture

### Service Layer
- **TranslationsAiService**: Main service interface for AI enhancement
- **OpenAiClient**: HTTP client for OpenAI API communication
- **PromptBuilder**: Constructs effective prompts for the AI model
- **DTOs**: PromptOptions and AiClientOptions for configuration

### UI Components
- **EnhanceDescriptionAction**: Backoffice action that appears in Product editor
- **TranslationsaiWidget**: Rich ZK widget for parameter input and preview
- **TranslationsaiController**: Widget controller handling user interactions

### Configuration
All beans are configured in `translationsai-backoffice-spring.xml`:
- `openAiClient`: OpenAI client with API key and base URL
- `translationsAiService`: Main AI service
- `enhanceDescriptionAction`: Backoffice action

## Configuration

### 1. Enable the Feature
Default configuration is provided in the extension's `project.properties` file. To enable and configure the feature, override these properties in `local.properties` or environment-specific properties:

```properties
# Enable AI enhancement
translationsai.enabled=true

# OpenAI API Configuration
translationsai.openai.apiKey=sk-your-actual-api-key-here
translationsai.openai.baseUrl=https://api.openai.com/v1
translationsai.openai.model=gpt-4o-mini

# AI Model Parameters
translationsai.temperature=0.3
translationsai.maxTokens=512
translationsai.timeout.ms=20000
```

**Note**: Default values are stored in `translationsai/project.properties`. Override them in `local.properties` for your environment.

### 2. Obtain OpenAI API Key
1. Create an account at https://platform.openai.com/
2. Generate an API key from the API keys section
3. Replace `YOUR_OPENAI_API_KEY_HERE` with your actual key

**Security Note**: For production, use encrypted properties or environment variables instead of plain text API keys.

### 3. Restart the Platform
After configuration, restart SAP Commerce to load the new settings.

## Usage

### Using the Backoffice Action

1. Open the Backoffice and navigate to a Product
2. In the Product editor toolbar, click "AI Enhance Description"
3. A dialog will show:
   - Current description
   - AI-enhanced version
4. Click "OK" to apply or "Cancel" to discard

### Using the Widget (Optional)

The widget provides more control:

1. Open the widget (can be added to Product perspective)
2. View the current description
3. Configure options:
   - **Tone**: Select the desired writing style
   - **Max Length**: Optionally limit word count
   - **Preserve Technical Terms**: Keep technical specifications intact
4. Click "Enhance with AI"
5. Review the enhanced description in the preview panel
6. Click "Apply" to save or "Try Again" to regenerate

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
│   │   └── EnhanceDescriptionAction.java
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
