"""
Book Details Page
View complete information about a book including reviews
"""

import streamlit as st
from utils.auth import init_session_state, is_authenticated
from utils.api_client import APIClient
from utils.helpers import render_star_rating
from components.navbar import render_navbar
from components.footer import render_footer

# Page configuration
st.set_page_config(
    page_title="Books.to.scrape Clone - Book Details",
    page_icon="📖",
    initial_sidebar_state="collapsed",
    layout="wide"
)

# Initialize
init_session_state()
render_navbar()

api = APIClient()

# Get selected book
book_id = st.session_state.get('selected_book_id')
book = st.session_state.get('selected_book_data', {})

if not book_id:
    st.warning("No book selected. Please go back to Browse Books.")
    if st.button("← Back to Browse Books"):
        st.switch_page("app.py")
    st.stop()

# Back button at top
if st.button("← Back to Browse Books"):
    st.switch_page("app.py")

st.divider()

# Book title
st.title(f"📖 {book.get('title', 'Book Details')}")

# Book information
col1, col2 = st.columns([2, 1])

with col1:
    # Category
    if book.get('categoryName'):
        st.markdown(f"**📁 Category:** {book['categoryName']}")

    # ISBN
    st.markdown(f"**📖 ISBN:** {book.get('isbn', 'N/A')}")

    st.divider()

    # Description
    if book.get('description'):
        st.subheader("Description")
        st.write(book['description'])

    # Book cover link
    if book.get('bookCoverImage'):
        st.markdown(f"[🔗 View book cover on original site]({book['bookCoverImage']})")

with col2:
    # Price
    if book.get('priceIncVat'):
        price_str = f"{book['currency']}{book['priceIncVat']:.2f}"
        st.metric("💰 Price", price_str)

    # Stock
    if book.get('availabilityStatus') and book.get('stockStatus', 0) > 0:
        st.metric("📦 In Stock", book['stockStatus'])
        st.success("✓ Available")
    else:
        st.metric("📦 Stock", "Out of Stock")
        st.error("✗ Not Available")

st.divider()

# Reviews section
st.header("⭐ Customer Reviews")

try:
    reviews_data = api.get_reviews_by_book_id(book_id, page=0, size=20)
    reviews = reviews_data.get('content', [])

    if reviews:
        # Calculate average rating
        avg_rating = sum(r['rating'] for r in reviews) / len(reviews)

        col1, col2 = st.columns([1, 3])
        with col1:
            st.metric("Average Rating", f"{avg_rating:.1f}/5")
            st.write(render_star_rating(int(avg_rating)))

        with col2:
            st.metric("Total Reviews", len(reviews))

        st.divider()

        # Display reviews
        for review in reviews:
            with st.container(border=True):
                col1, col2 = st.columns([3, 1])

                with col1:
                    st.markdown(f"**{review.get('reviewerName', 'Anonymous')}**")
                    st.caption(f"Posted on {review.get('createdAt', 'Unknown date')}")

                with col2:
                    st.write(render_star_rating(review['rating']))
                    st.caption(f"{review['rating']}/5")

                st.write(review.get('review', review.get('comment', 'No review text')))

    else:
        st.info("📝 No reviews yet. Be the first to review this book!")

except Exception as e:
    st.error(f"Error loading reviews: {e}")

st.divider()

# Write review section
if is_authenticated():
    st.subheader("✍️ Write Your Review")

    with st.form("review_form"):
        rating = st.slider("⭐ Rating", 1, 5, 5, help="1 = Poor, 5 = Excellent")
        comment = st.text_area(
            "📝 Your Review",
            placeholder="Share your thoughts about this book...",
            height=150
        )

        col1, col2 = st.columns([1, 4])
        with col1:
            submit = st.form_submit_button("Submit Review", use_container_width=True)

        if submit:
            if not comment or len(comment.strip()) < 10:
                st.error("Please write at least 10 characters in your review")
            else:
                try:
                    api.create_review(book_id, rating, comment)
                    st.success("✅ Review submitted successfully!")
                    st.info("Refresh the page to see your review")
                    st.rerun()
                except Exception as e:
                    st.error(f"❌ Error submitting review: {e}")
else:
    st.info("🔒 Please login to write a review")
    st.caption("Use the sidebar to login or register")

# Footer
render_footer()
