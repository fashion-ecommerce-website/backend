# Daily Report API

## Overview
This API allows you to send a daily report PDF via email containing all orders from today.

## Endpoint
```
POST /api/reports/daily
```

## Request Body
```json
{
  "email": "recipient@example.com"
}
```

## Response
- **Success (200)**: `"Daily report sent successfully to recipient@example.com"`
- **Error (400)**: Invalid email format
- **Error (500)**: Server error with error message

## Features
- Generates a PDF report with all orders from today
- Includes order details, customer information, and totals
- Sends the PDF as an email attachment
- Supports Vietnamese language in the report

## Example Usage

### cURL
```bash
curl -X POST http://localhost:8080/api/reports/daily \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@company.com"}'
```

### JavaScript
```javascript
fetch('/api/reports/daily', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'admin@company.com'
  })
})
.then(response => response.text())
.then(data => console.log(data));
```

## Report Contents
The generated PDF includes:
- Report date
- Total number of orders
- Total revenue
- Detailed order table with:
  - Order ID
  - Customer name and email
  - Order status
  - Payment status
  - Total amount
  - Creation time

## Dependencies Added
- iText7 for PDF generation
- HTML2PDF for converting HTML to PDF
- Enhanced email service for sending attachments
