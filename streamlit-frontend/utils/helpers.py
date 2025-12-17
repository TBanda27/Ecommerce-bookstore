"""
Helper utilities for Streamlit frontend
"""

import streamlit as st
from datetime import datetime
from typing import Optional


def format_date(date_str: str) -> str:
    """Format ISO datetime string to readable format"""
    try:
        dt = datetime.fromisoformat(date_str.replace('Z', '+00:00'))
        return dt.strftime('%B %d, %Y at %I:%M %p')
    except:
        return date_str


def format_currency(amount: float, currency: str = "USD") -> str:
    """Format currency amount"""
    symbols = {
        "USD": "$",
        "EUR": "€",
        "GBP": "£",
        "INR": "₹"
    }
    symbol = symbols.get(currency, currency)
    return f"{symbol}{amount:.2f}"


def render_star_rating(rating: int, max_rating: int = 5) -> str:
    """Render star rating as stars"""
    filled_stars = "⭐" * rating
    empty_stars = "☆" * (max_rating - rating)
    return filled_stars + empty_stars


def show_success(message: str):
    """Show success message with auto-dismiss"""
    st.success(message)


def show_error(message: str):
    """Show error message"""
    st.error(message)


def show_info(message: str):
    """Show info message"""
    st.info(message)


def paginate_data(data: dict, page_key: str = "page"):
    """Helper to handle pagination controls"""
    content = data.get('content', [])
    total_pages = data.get('totalPages', 1)
    current_page = data.get('number', 0)
    total_elements = data.get('totalElements', 0)

    st.caption(f"Total: {total_elements} items")

    if total_pages > 1:
        col1, col2, col3 = st.columns([1, 2, 1])

        with col1:
            if current_page > 0:
                if st.button("← Previous", key=f"{page_key}_prev"):
                    st.session_state[page_key] = current_page - 1
                    st.rerun()

        with col2:
            st.write(f"Page {current_page + 1} of {total_pages}")

        with col3:
            if current_page < total_pages - 1:
                if st.button("Next →", key=f"{page_key}_next"):
                    st.session_state[page_key] = current_page + 1
                    st.rerun()

    return content


def init_page_state(page_key: str):
    """Initialize page number in session state"""
    if page_key not in st.session_state:
        st.session_state[page_key] = 0
