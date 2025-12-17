"""
My Reviews Page
View and manage user's reviews
"""

import streamlit as st
from utils.auth import init_session_state, require_auth
from utils.api_client import APIClient
from utils.helpers import render_star_rating, format_date, init_page_state
from components.navbar import render_navbar
from components.footer import render_footer

# Page configuration
st.set_page_config(
    page_title="Books.to.scrape Clone - My Reviews",
    page_icon="⭐",
    layout="wide",
    initial_sidebar_state="collapsed"
)

# Initialize
init_session_state()
init_page_state("reviews_page")
require_auth()
render_navbar()

st.title("⭐ My Reviews")

api = APIClient()

# Sort options
col1, col2, col3 = st.columns([2, 2, 2])

with col1:
    sort_by = st.selectbox(
        "Sort by",
        ["createdAt", "rating", "updatedAt"],
        format_func=lambda x: {
            "createdAt": "Date Created",
            "rating": "Rating",
            "updatedAt": "Last Updated"
        }[x]
    )

with col2:
    sort_dir = st.selectbox("Order", ["desc", "asc"], format_func=lambda x: "Newest First" if x == "desc" else "Oldest First")

with col3:
    page_size = st.selectbox("Reviews per page", [5, 10, 20], index=1)

st.divider()

# Fetch reviews
try:
    current_page = st.session_state.get("reviews_page", 0)

    reviews_data = api.get_my_reviews(
        page=current_page,
        size=page_size,
        sort_by=sort_by,
        sort_dir=sort_dir
    )

    reviews = reviews_data.get('content', [])
    total_reviews = reviews_data.get('totalElements', 0)

    st.caption(f"Total reviews: {total_reviews}")

    if reviews:
        for review in reviews:
            with st.container(border=True):
                # Header with book info and rating
                col1, col2 = st.columns([3, 1])

                with col1:
                    # Display book name from review response
                    book_name = review.get('bookName', 'Unknown Book')
                    st.subheader(f"📖 {book_name}")

                with col2:
                    st.write(render_star_rating(review['rating']))
                    st.caption(f"Rating: {review['rating']}/5")

                # Review content
                st.write(review.get('review', review.get('comment', 'No review text')))

                # Metadata
                col1, col2, col3 = st.columns([2, 2, 2])

                with col1:
                    st.caption(f"Posted: {format_date(review.get('createdAt', ''))}")

                with col2:
                    if review.get('updatedAt') != review.get('createdAt'):
                        st.caption(f"Updated: {format_date(review.get('updatedAt', ''))}")

                # Actions
                col1, col2 = st.columns([1, 5])

                with col1:
                    if st.button("✏️ Edit", key=f"edit_{review['id']}"):
                        st.session_state[f"editing_{review['id']}"] = True
                        st.rerun()

                with col2:
                    if st.button("🗑️ Delete", key=f"delete_{review['id']}"):
                        try:
                            api.delete_review(review['id'])
                            st.success("Review deleted!")
                            st.rerun()
                        except Exception as e:
                            st.error(f"Error deleting review: {e}")

                # Edit form (if editing)
                if st.session_state.get(f"editing_{review['id']}", False):
                    st.divider()
                    st.subheader("Edit Review")

                    with st.form(f"edit_form_{review['id']}"):
                        new_rating = st.slider("Rating", 0, 5, review['rating'], help="0-5 stars (0 = no rating)")
                        new_comment = st.text_area("Review", value=review.get('review', review.get('comment', '')))

                        col1, col2 = st.columns(2)

                        with col1:
                            save = st.form_submit_button("Save Changes", use_container_width=True)

                        with col2:
                            cancel = st.form_submit_button("Cancel", use_container_width=True)

                        if save:
                            try:
                                api.update_review(review['id'], review['bookId'], new_rating, new_comment)
                                st.success("Review updated!")
                                del st.session_state[f"editing_{review['id']}"]
                                st.rerun()
                            except Exception as e:
                                st.error(f"Error updating review: {e}")

                        if cancel:
                            del st.session_state[f"editing_{review['id']}"]
                            st.rerun()

        # Pagination
        st.divider()
        total_pages = reviews_data.get('totalPages', 1)

        if total_pages > 1:
            col1, col2, col3 = st.columns([1, 2, 1])

            with col1:
                if current_page > 0:
                    if st.button("← Previous"):
                        st.session_state["reviews_page"] = current_page - 1
                        st.rerun()

            with col2:
                st.write(f"Page {current_page + 1} of {total_pages}")

            with col3:
                if current_page < total_pages - 1:
                    if st.button("Next →"):
                        st.session_state["reviews_page"] = current_page + 1
                        st.rerun()

    else:
        st.info("You haven't written any reviews yet.")
        st.write("Browse books and share your thoughts!")

        if st.button("📚 Browse Books"):
            st.switch_page("app.py")

except Exception as e:
    st.error(f"Error loading reviews: {e}")

# Footer
render_footer()
