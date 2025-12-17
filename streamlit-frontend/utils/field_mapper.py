"""
Field mapper to normalize backend API responses
Maps backend field names to frontend-expected field names
"""

def normalize_book(book: dict) -> dict:
    """Normalize book object from backend API"""
    # Your backend structure from books.toscrape.com
    return {
        'id': book.get('bookId', book.get('id')),
        'title': book.get('name', book.get('title', 'Untitled')),
        'author': book.get('author', ''),  # Not available in your backend
        'isbn': book.get('uniqueProductCode', book.get('isbn', 'N/A')),
        'description': book.get('description', ''),
        'publisher': book.get('publisher', ''),  # Not available
        'publicationYear': book.get('publicationYear', ''),  # Not available
        'categoryId': book.get('categoryId'),
        'categoryName': book.get('categoryName', ''),
        'bookCoverImage': book.get('bookCoverImage', ''),
        'priceExclVat': book.get('priceExclVat', 0),
        'priceIncVat': book.get('priceIncVat', 0),
        'currency': book.get('currency', '£'),
        'stockStatus': book.get('stockStatus', 0),
        'availabilityStatus': book.get('availabilityStatus', False),
        'numberOfReviews': book.get('numberOfReviews', 0),
        # Original fields for reference
        '_bookId': book.get('bookId'),
        '_name': book.get('name')
    }


def normalize_category(category: dict) -> dict:
    """Normalize category object from backend API"""
    return {
        'id': category.get('id'),
        'name': category.get('categoryName', category.get('name', 'Unknown')),
        'description': category.get('description', ''),
        # Original fields
        '_categoryName': category.get('categoryName')
    }


def normalize_books_list(books: list) -> list:
    """Normalize list of books"""
    return [normalize_book(book) for book in books]


def normalize_categories_list(categories: list) -> list:
    """Normalize list of categories"""
    return [normalize_category(cat) for cat in categories]


def normalize_review(review: dict) -> dict:
    """Normalize review object from backend API"""
    return {
        'id': review.get('id'),
        'bookId': review.get('bookId'),
        'bookName': review.get('bookName', 'Unknown Book'),
        'rating': review.get('rating', 0),
        'review': review.get('review', review.get('comment', '')),
        'createdAt': review.get('createdAt', ''),
        'updatedAt': review.get('updatedAt', ''),
        'reviewerName': review.get('reviewerName', 'Anonymous'),
        # Original fields for reference
        '_review': review.get('review'),
        '_comment': review.get('comment')
    }


def normalize_reviews_list(reviews: list) -> list:
    """Normalize list of reviews"""
    return [normalize_review(review) for review in reviews]
