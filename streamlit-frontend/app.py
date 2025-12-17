"""
Books E-commerce Store - Browse Books
Main page showing all available books
"""

import streamlit as st
from utils.auth import init_session_state, is_authenticated, set_auth_data
from utils.api_client import APIClient
from utils.helpers import init_page_state
from utils.field_mapper import normalize_books_list, normalize_categories_list
from components.navbar import render_navbar
from components.footer import render_footer
import time

# Page configuration
st.set_page_config(
    page_title="A books.toscrape.com clone - Browse Books",
    page_icon="📚",
    layout="wide",
    initial_sidebar_state="collapsed"
)

# Initialize
init_session_state()

# Handle OAuth2 callback (if redirected from Google login)
query_params = st.query_params
if "token" in query_params and "username" in query_params and "email" in query_params:
    try:
        # Extract OAuth2 login data from URL parameters
        token = query_params["token"]
        username = query_params["username"]
        email = query_params["email"]

        # Prepare login response format
        login_response = {
            "token": token,
            "username": username,
            "email": email,
            "type": "Bearer",
            "authProvider": "google"
        }

        # Set authentication data in session
        set_auth_data(login_response)

        st.success(f"✓ Successfully logged in with Google as **{username}**!")
        st.info("Redirecting...")

        # Clear query params
        st.query_params.clear()

        # Auto-redirect after 2 seconds
        time.sleep(2)
        st.rerun()

    except Exception as e:
        st.error(f"OAuth2 login failed: {e}")

init_page_state("browse_page")
render_navbar()

st.title("📚 Browse Books")

api = APIClient()

# Filters section
col1, col2, col3 = st.columns([2, 2, 1])

with col1:
    # Category filter
    try:
        categories_data = api.get_categories(page=0, size=100)
        categories = normalize_categories_list(categories_data.get('content', []))
        category_options = {"All Categories": None}
        for cat in categories:
            category_options[cat['name']] = cat['id']

        selected_category = st.selectbox(
            "Filter by Category",
            options=list(category_options.keys()),
            key="category_filter"
        )
        category_id = category_options[selected_category]
    except Exception as e:
        st.error(f"Error loading categories: {e}")
        category_id = None

with col2:
    search_term = st.text_input("🔍 Search books", placeholder="Enter title...")

with col3:
    page_size = st.selectbox("Items per page", [9, 18, 36], index=0)

st.divider()

# Fetch books
try:
    current_page = st.session_state.get("browse_page", 0)

    books_data = api.get_books(
        page=current_page,
        size=page_size,
        category_id=category_id
    )

    books = normalize_books_list(books_data.get('content', []))

    # Client-side filtering by search term
    if search_term:
        books = [
            book for book in books
            if search_term.lower() in book['title'].lower()
        ]

    if books:
        st.caption(f"Showing {len(books)} books")

        # Display books in grid (3 columns)
        for i in range(0, len(books), 3):
            cols = st.columns(3)

            for j, col in enumerate(cols):
                if i + j < len(books):
                    book = books[i + j]

                    with col:
                        with st.container(border=True, height=420):
                            # Title (fixed height - max 2 lines)
                            title = book['title']
                            if len(title) > 60:
                                title = title[:60] + "..."
                            st.markdown(f"### {title}")

                            # Category
                            if book.get('categoryName'):
                                st.caption(f"📁 {book['categoryName']}")
                            else:
                                st.caption("📁 Uncategorized")

                            st.write("")  # spacing

                            # Description (fixed 3 lines = ~150 chars)
                            desc = book.get('description', 'No description available.')
                            if len(desc) > 150:
                                desc = desc[:150] + "..."
                            st.markdown(f"<div style='height: 80px; overflow: hidden;'>{desc}</div>", unsafe_allow_html=True)

                            st.divider()

                            # Price
                            if book.get('priceIncVat'):
                                price_str = f"{book['currency']}{book['priceIncVat']:.2f}"
                                st.markdown(f"**💰 Price:** {price_str}")
                            else:
                                st.markdown("**💰 Price:** Not available")

                            # Stock quantity
                            if book.get('availabilityStatus') and book.get('stockStatus', 0) > 0:
                                st.markdown(f"**📦 Stock:** {book['stockStatus']} units")
                            else:
                                st.markdown("**📦 Stock:** Out of stock")

                            # Reviews count
                            num_reviews = book.get('numberOfReviews', 0)
                            st.caption(f"⭐ {num_reviews} review{'s' if num_reviews != 1 else ''}")

                            st.write("")  # spacing

                            # View details button
                            if st.button("📖 View Details", key=f"view_{book['id']}", use_container_width=True):
                                st.session_state['selected_book_id'] = book['id']
                                st.session_state['selected_book_data'] = book
                                st.switch_page("pages/1_📖_Book_Details.py")

        # Pagination
        st.divider()
        total_pages = books_data.get('totalPages', 1)
        current_page = books_data.get('number', 0)

        if total_pages > 1:
            col1, col2, col3 = st.columns([1, 2, 1])

            with col1:
                if current_page > 0:
                    if st.button("← Previous"):
                        st.session_state["browse_page"] = current_page - 1
                        st.rerun()

            with col2:
                st.write(f"Page {current_page + 1} of {total_pages}")

            with col3:
                if current_page < total_pages - 1:
                    if st.button("Next →"):
                        st.session_state["browse_page"] = current_page + 1
                        st.rerun()

    else:
        st.info("No books found matching your criteria.")

except Exception as e:
    st.error(f"Error loading books: {e}")

# Footer
render_footer()
