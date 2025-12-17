# Streamlit App Structure

## 📁 Page Layout

### Main Page (Default)
- **File**: `app.py`
- **URL**: `http://localhost:8501/`
- **Purpose**: Browse Books - Main catalog view
- **Features**:
  - Category filter dropdown
  - Search by title
  - Items per page selector (9, 18, 36)
  - Grid layout (3 columns)
  - Shows: Title, Category, Brief Description, Price, Stock Status
  - Pagination controls
  - Click "View Details" → Goes to Book Details page

### Additional Pages

1. **Book Details** (`pages/1_📖_Book_Details.py`)
   - Full book information
   - Complete description
   - All reviews with ratings
   - Write review form (if logged in)
   - Back button to browse

2. **Register** (`pages/2_📝_Register.py`)
   - User registration form
   - Email verification flow

3. **My Profile** (`pages/3_👤_Profile.py`)
   - View/edit account info
   - Delete account option

4. **My Reviews** (`pages/4_⭐_My_Reviews.py`)
   - All user's reviews
   - Edit/delete reviews

5. **Admin Panel** (`pages/5_🛠️_Admin_Panel.py`)
   - Manage books, categories, prices, inventory, users
   - Admin-only access

## 🎨 Book Card Display Format

Each book card shows:
```
┌─────────────────────────────┐
│ Book Title                  │
│ 📁 Category Name            │
│                             │
│ Brief description (120      │
│ chars)...                   │
│ ─────────────────────────   │
│ 💰 £45.17    ✓ In Stock    │
│ ⭐ 5 reviews                │
│                             │
│   [📖 View Details]         │
└─────────────────────────────┘
```

## 🔑 Fields Displayed

### Browse View (Main Page):
- ✅ Title
- ✅ Category
- ✅ Description (truncated to 120 chars)
- ✅ Price (with currency symbol)
- ✅ Stock status
- ✅ Number of reviews

### Book Details Page:
- ✅ Title
- ✅ Category
- ✅ ISBN
- ✅ Full description
- ✅ Price
- ✅ Stock quantity
- ✅ Number of reviews
- ✅ Book cover link
- ✅ All reviews with ratings
- ✅ Write review form

## 🚀 Quick Start

```bash
cd "D:\Spring Boot\Books-Ecommerce Project\streamlit-frontend"
streamlit run app.py
```

**Default URL**: `http://localhost:8501/`

## 📊 Data Source

- Backend: Spring Boot microservices
- API Gateway: `http://localhost:9090`
- Book data: 1000 books from books.toscrape.com
- Categories: 50 categories
- All data embedded in book response (no extra API calls needed)
