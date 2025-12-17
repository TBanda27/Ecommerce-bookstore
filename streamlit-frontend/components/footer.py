"""
Footer component for all pages
"""

import streamlit as st


def render_footer():
    """Render footer with attribution"""
    st.divider()

    # Center-aligned footer
    footer_html = """
    <div style="text-align: center; padding: 20px 0; color: #666;">
        <p>📚 A <strong>Books.to.scrape</strong> clone with enhanced features</p>
        <p style="margin-top: 10px;">Built by <strong>tbanda27</strong> with <a href="https://streamlit.io" target="_blank" style="color: #FF4B4B; text-decoration: none;">Streamlit</a></p>
    </div>
    """

    st.markdown(footer_html, unsafe_allow_html=True)
