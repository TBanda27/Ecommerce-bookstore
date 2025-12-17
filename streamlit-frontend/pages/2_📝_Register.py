"""
User Registration Page
"""

import streamlit as st
from utils.auth import init_session_state, is_authenticated
from utils.api_client import APIClient
from components.navbar import render_navbar
from components.footer import render_footer

# Page configuration
st.set_page_config(
    page_title="Books.to.scrape Clone - Register",
    page_icon="📝",
    layout="wide",
    initial_sidebar_state="collapsed"
)

# Initialize
init_session_state()
render_navbar()

st.title("📝 Create Your Account")

if is_authenticated():
    st.success(f"You're already logged in as {st.session_state.user}")
    st.info("Go to Browse Books to start exploring!")
    if st.button("Browse Books"):
        st.switch_page("app.py")
else:
    st.write("Join our community of book lovers!")

    api = APIClient()

    with st.form("registration_form"):
        username = st.text_input(
            "Username",
            placeholder="Choose a unique username",
            help="This will be displayed on your reviews"
        )

        email = st.text_input(
            "Email",
            placeholder="your.email@example.com",
            help="We'll send a verification email to this address"
        )

        col1, col2 = st.columns(2)

        with col1:
            password = st.text_input(
                "Password",
                type="password",
                placeholder="Enter a strong password"
            )

        with col2:
            confirm_password = st.text_input(
                "Confirm Password",
                type="password",
                placeholder="Re-enter your password"
            )

        st.divider()

        agree = st.checkbox("I agree to the Terms of Service and Privacy Policy")

        submit = st.form_submit_button("Create Account", use_container_width=True)

        if submit:
            # Validation
            errors = []

            if not username or len(username) < 3:
                errors.append("Username must be at least 3 characters long")

            if not email or "@" not in email:
                errors.append("Please enter a valid email address")

            if not password or len(password) < 6:
                errors.append("Password must be at least 6 characters long")

            if password != confirm_password:
                errors.append("Passwords do not match")

            if not agree:
                errors.append("You must agree to the Terms of Service")

            if errors:
                for error in errors:
                    st.error(error)
            else:
                try:
                    result = api.register(username, email, password, confirm_password)
                    st.success("✅ Registration successful!")

                    st.info(f"📧 A verification email has been sent to **{email}**")

                    st.warning("""
                    **⚠️ IMPORTANT - Next Steps:**

                    1. 📧 Check your email inbox (and spam folder)
                    2. 🔗 Click the verification link in the email
                       - This will open a backend page (localhost:9090) - that's normal!
                    3. ✅ After verification, **CLOSE THAT TAB** or come back here
                    4. 🔐 Use the **SIDEBAR** on the left to LOGIN with your credentials
                    5. 📚 Start exploring and reviewing books!
                    """)

                    with st.expander("❓ Didn't receive the email?"):
                        st.markdown("""
                        - Check your spam/junk folder
                        - Wait a few minutes and refresh your inbox
                        - Request a new verification email below
                        """)

                    # Resend verification option
                    st.divider()
                    st.subheader("Resend Verification Email")

                    with st.form("resend_form"):
                        resend_email = st.text_input("Email", value=email)
                        resend_submit = st.form_submit_button("Resend Verification")

                        if resend_submit:
                            try:
                                api.resend_verification(resend_email)
                                st.success("Verification email sent!")
                            except Exception as e:
                                st.error(f"Error: {e}")

                except Exception as e:
                    st.error(f"Registration failed: {e}")

    st.divider()

    st.info("Already have an account? Use the sidebar to login!")

# Footer
render_footer()
