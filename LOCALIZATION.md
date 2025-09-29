# Localization Guide

## Overview
The app supports multiple languages through a combination of:
1. Local `strings.xml` for UI labels
2. Backend-provided localized content based on device language

## How It Works

### 1. Device Language Detection
- The app automatically detects the device language using `Locale.getDefault()`
- Language code (e.g., "tr", "en") is sent with every API request via `Accept-Language` header

### 2. API Request Flow
```kotlin
// LanguageInterceptor automatically adds Accept-Language header
GET /slider
Headers:
  Accept-Language: tr  // Device language
```

### 3. Backend Implementation
The backend should:
- Read `Accept-Language` header from request
- Return localized content based on language code
- Default to English if language not supported

#### Example Backend Logic (Node.js/Express):
```javascript
app.get('/slider', (req, res) => {
  const lang = req.headers['accept-language'] || 'en';
  
  const sliders = [
    {
      id: 's1',
      imageUrl: 'https://cdn.example.com/slide1.jpg',
      title: lang === 'tr' ? 'Yaz Kampı' : 'Summer Camp',
      order: 1
    },
    {
      id: 's2',
      imageUrl: 'https://cdn.example.com/slide2.jpg',
      title: lang === 'tr' ? 'At Biniciliği' : 'Horse Riding',
      order: 2
    }
  ];
  
  res.json(sliders);
});
```

### 4. Supported Languages
- **Turkish (tr)**: Primary language
- **English (en)**: Default fallback
- Add more languages in `values-{lang}/strings.xml`

## Adding New Language Support

### 1. Add strings.xml for new language
```
core/src/main/res/values-{lang}/strings.xml
```

### 2. Update backend to support new language
Check `Accept-Language` header and return appropriate content.

### 3. No app code changes needed
The `LanguageInterceptor` automatically sends device language.

## Testing

### Change Device Language
1. Settings → Language & Input → Languages
2. Add/Select language
3. App automatically uses new language on next API call

### Mock API Testing
For local development, you can:
1. Modify `LanguageInterceptor` to force specific language
2. Or use backend that reads `Accept-Language` header

## Key Files
- `data/src/main/java/com/example/data/remote/LanguageInterceptor.kt` - Adds language header
- `data/src/main/java/com/example/data/di/NetworkModule.kt` - Configures interceptor
- `core/src/main/res/values/strings.xml` - English UI strings
- `core/src/main/res/values-tr/strings.xml` - Turkish UI strings
