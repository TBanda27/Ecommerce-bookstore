"""
API Client for Books E-commerce Backend
Handles all HTTP requests to the Spring Boot microservices via API Gateway
"""

import requests
import streamlit as st
from typing import Optional, Dict, Any
import os
from dotenv import load_dotenv

load_dotenv()


class APIClient:
    """Client for interacting with the Books E-commerce API Gateway"""

    def __init__(self):
        # Try to get from Streamlit secrets first (for cloud deployment)
        # Fall back to environment variable for local development
        try:
            self.base_url = st.secrets.get("API_GATEWAY_URL", os.getenv("API_GATEWAY_URL", "http://localhost:9090"))
        except (FileNotFoundError, AttributeError):
            # If secrets.toml doesn't exist (local dev), use env variable
            self.base_url = os.getenv("API_GATEWAY_URL", "http://localhost:9090")

    def _get_headers(self) -> Dict[str, str]:
        """Get headers including JWT token if authenticated"""
        headers = {"Content-Type": "application/json"}
        if 'token' in st.session_state and st.session_state.token:
            headers["Authorization"] = f"Bearer {st.session_state.token}"
        return headers

    def _handle_response(self, response: requests.Response) -> Dict[str, Any]:
        """Handle API response and errors"""
        try:
            if response.status_code == 401:
                st.session_state.token = None
                st.session_state.user = None
                raise Exception("Session expired. Please login again.")

            response.raise_for_status()
            return response.json() if response.content else {}
        except requests.exceptions.HTTPError as e:
            error_msg = "An error occurred"
            try:
                error_data = response.json()
                error_msg = error_data.get('message', error_data.get('error', str(e)))
            except:
                error_msg = str(e)
            raise Exception(error_msg)

    # ==================== AUTHENTICATION ====================

    def login(self, email: str, password: str) -> Dict[str, Any]:
        """Login user and get JWT token"""
        response = requests.post(
            f"{self.base_url}/api/v1/auth/login",
            json={"email": email, "password": password}
        )
        return self._handle_response(response)

    def get_oauth2_login_url(self) -> str:
        """Get Google OAuth2 login URL"""
        return f"{self.base_url}/api/v1/oauth2/login/google"

    def register(self, username: str, email: str, password: str, confirm_password: str = None) -> Dict[str, Any]:
        """Register a new user"""
        # If confirm_password not provided, use password (frontend already validated they match)
        if confirm_password is None:
            confirm_password = password

        response = requests.post(
            f"{self.base_url}/api/v1/user",
            json={
                "username": username,
                "email": email,
                "password": password,
                "confirmPassword": confirm_password
            },
            allow_redirects=False  # Don't follow redirects to prevent redirect loops
        )
        # Check for unexpected redirects
        if response.status_code in [301, 302, 303, 307, 308]:
            raise Exception("Registration endpoint returned a redirect. Please restart the auth-service and try again.")
        return self._handle_response(response)

    def verify_email(self, token: str) -> Dict[str, Any]:
        """Verify email with verification token"""
        response = requests.get(
            f"{self.base_url}/api/v1/auth/verify",
            params={"token": token}
        )
        return self._handle_response(response)

    def resend_verification(self, email: str) -> Dict[str, Any]:
        """Resend verification email"""
        response = requests.post(
            f"{self.base_url}/api/v1/auth/resend-verification",
            params={"email": email}
        )
        return self._handle_response(response)

    # ==================== USER MANAGEMENT ====================

    def get_current_user(self) -> Dict[str, Any]:
        """Get current logged-in user profile"""
        response = requests.get(
            f"{self.base_url}/api/v1/user/me",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def update_current_user(self, username: str, email: str) -> Dict[str, Any]:
        """Update current user profile"""
        response = requests.put(
            f"{self.base_url}/api/v1/user/me",
            json={"username": username, "email": email},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def delete_current_user(self) -> Dict[str, Any]:
        """Delete current user account"""
        response = requests.delete(
            f"{self.base_url}/api/v1/user/me",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def get_all_users(self, page: int = 0, size: int = 10) -> Dict[str, Any]:
        """Get all users (admin only)"""
        response = requests.get(
            f"{self.base_url}/api/v1/user",
            params={"page": page, "size": size},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def get_user_by_id(self, user_id: int) -> Dict[str, Any]:
        """Get user by ID (admin only)"""
        response = requests.get(
            f"{self.base_url}/api/v1/user/{user_id}",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def delete_user_by_id(self, user_id: int) -> Dict[str, Any]:
        """Delete user by ID (admin only)"""
        response = requests.delete(
            f"{self.base_url}/api/v1/user/{user_id}",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    # ==================== BOOKS ====================

    def get_books(self, page: int = 0, size: int = 10, category_id: Optional[int] = None) -> Dict[str, Any]:
        """Get all books with optional category filter"""
        params = {"page": page, "size": size}
        if category_id:
            params["categoryId"] = category_id

        response = requests.get(
            f"{self.base_url}/api/v1/books",
            params=params
        )
        return self._handle_response(response)

    def get_book_by_id(self, book_id: int) -> Dict[str, Any]:
        """Get book by ID"""
        response = requests.get(f"{self.base_url}/api/v1/books/{book_id}")
        return self._handle_response(response)

    def create_book(self, name: str, description: str, book_cover_image: str,
                   unique_product_code: str, category_id: int,
                   price_excl_vat: float, tax_amount: float, currency: str,
                   stock_quantity: int, availability_status: bool) -> Dict[str, Any]:
        """Create a new book (admin only)"""
        response = requests.post(
            f"{self.base_url}/api/v1/books",
            json={
                "bookRequestDTO": {
                    "name": name,
                    "description": description,
                    "bookCoverImage": book_cover_image,
                    "uniqueProductCode": unique_product_code,
                    "categoryId": category_id
                },
                "priceDataDTO": {
                    "priceExclVat": price_excl_vat,
                    "taxAmount": tax_amount,
                    "currency": currency
                },
                "inventoryDataDTO": {
                    "stockQuantity": stock_quantity,
                    "availabilityStatus": availability_status
                }
            },
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def update_book(self, book_id: int, name: str, description: str,
                   book_cover_image: str, unique_product_code: str,
                   category_id: int) -> Dict[str, Any]:
        """Update book (admin only)"""
        response = requests.put(
            f"{self.base_url}/api/v1/books/{book_id}",
            json={
                "name": name,
                "description": description,
                "bookCoverImage": book_cover_image,
                "uniqueProductCode": unique_product_code,
                "categoryId": category_id
            },
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def delete_book(self, book_id: int) -> Dict[str, Any]:
        """Delete book (admin only)"""
        response = requests.delete(
            f"{self.base_url}/api/v1/books/{book_id}",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    # ==================== CATEGORIES ====================

    def get_categories(self, page: int = 0, size: int = 100) -> Dict[str, Any]:
        """Get all categories"""
        response = requests.get(
            f"{self.base_url}/api/v1/category",
            params={"page": page, "size": size}
        )
        return self._handle_response(response)

    def get_category_by_id(self, category_id: int) -> Dict[str, Any]:
        """Get category by ID"""
        response = requests.get(f"{self.base_url}/api/v1/category/{category_id}")
        return self._handle_response(response)

    def create_category(self, category_name: str) -> Dict[str, Any]:
        """Create category (admin only)"""
        response = requests.post(
            f"{self.base_url}/api/v1/category",
            json={"categoryName": category_name},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def update_category(self, category_id: int, category_name: str) -> Dict[str, Any]:
        """Update category (admin only)"""
        response = requests.put(
            f"{self.base_url}/api/v1/category/{category_id}",
            json={"categoryName": category_name},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def delete_category(self, category_id: int) -> Dict[str, Any]:
        """Delete category (admin only)"""
        response = requests.delete(
            f"{self.base_url}/api/v1/category/{category_id}",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    # ==================== PRICE ====================

    def get_price_by_book_id(self, book_id: int) -> Dict[str, Any]:
        """Get price by book ID"""
        response = requests.get(f"{self.base_url}/api/v1/price/book/{book_id}")
        return self._handle_response(response)

    def create_price(self, book_id: int, price_excl_vat: float, tax_amount: float, currency: str) -> Dict[str, Any]:
        """Create price (admin only)"""
        response = requests.post(
            f"{self.base_url}/api/v1/price",
            json={
                "bookId": book_id,
                "priceExclVat": price_excl_vat,
                "taxAmount": tax_amount,
                "currency": currency
            },
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def update_price(self, price_id: int, book_id: int, price_excl_vat: float, tax_amount: float, currency: str) -> Dict[str, Any]:
        """Update price (admin only)"""
        response = requests.put(
            f"{self.base_url}/api/v1/price/{price_id}",
            json={
                "bookId": book_id,
                "priceExclVat": price_excl_vat,
                "taxAmount": tax_amount,
                "currency": currency
            },
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def delete_price_by_book_id(self, book_id: int) -> Dict[str, Any]:
        """Delete price by book ID (admin only)"""
        response = requests.delete(
            f"{self.base_url}/api/v1/price/book/{book_id}",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    # ==================== INVENTORY ====================

    def get_inventory_by_book_id(self, book_id: int) -> Dict[str, Any]:
        """Get inventory by book ID"""
        response = requests.get(f"{self.base_url}/api/v1/inventory/book/{book_id}")
        return self._handle_response(response)

    def get_all_inventory(self, page: int = 0, size: int = 10) -> Dict[str, Any]:
        """Get all inventory"""
        response = requests.get(
            f"{self.base_url}/api/v1/inventory",
            params={"page": page, "size": size}
        )
        return self._handle_response(response)

    def create_inventory(self, book_id: int, quantity: int, location: str = "Warehouse A") -> Dict[str, Any]:
        """Create inventory (admin only)"""
        response = requests.post(
            f"{self.base_url}/api/v1/inventory",
            json={"bookId": book_id, "stockQuantity": quantity, "availabilityStatus": quantity > 0},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def update_inventory(self, inventory_id: int, book_id: int, quantity: int, availability_status: bool) -> Dict[str, Any]:
        """Update inventory (admin only)"""
        response = requests.put(
            f"{self.base_url}/api/v1/inventory/{inventory_id}",
            json={
                "bookId": book_id,
                "stockQuantity": quantity,
                "availabilityStatus": availability_status
            },
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def delete_inventory_by_book_id(self, book_id: int) -> Dict[str, Any]:
        """Delete inventory by book ID (admin only)"""
        response = requests.delete(
            f"{self.base_url}/api/v1/inventory/book/{book_id}",
            headers=self._get_headers()
        )
        return self._handle_response(response)

    # ==================== REVIEWS ====================

    def get_reviews_by_book_id(self, book_id: int, page: int = 0, size: int = 10,
                               sort_by: str = "createdAt", sort_dir: str = "desc") -> Dict[str, Any]:
        """Get reviews for a specific book"""
        response = requests.get(
            f"{self.base_url}/api/v1/review/book/{book_id}",
            params={"page": page, "size": size, "sortBy": sort_by, "sortDir": sort_dir}
        )
        return self._handle_response(response)

    def get_my_reviews(self, page: int = 0, size: int = 10,
                      sort_by: str = "createdAt", sort_dir: str = "desc") -> Dict[str, Any]:
        """Get current user's reviews"""
        response = requests.get(
            f"{self.base_url}/api/v1/review/me",
            params={"page": page, "size": size, "sortBy": sort_by, "sortDir": sort_dir},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def create_review(self, book_id: int, rating: int, comment: str) -> Dict[str, Any]:
        """Create a review (authenticated users only)"""
        response = requests.post(
            f"{self.base_url}/api/v1/review",
            json={"bookId": book_id, "rating": rating, "review": comment},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def update_review(self, review_id: int, book_id: int, rating: int, comment: str) -> Dict[str, Any]:
        """Update own review (authenticated users only)"""
        response = requests.put(
            f"{self.base_url}/api/v1/review/{review_id}",
            json={"bookId": book_id, "rating": rating, "review": comment},
            headers=self._get_headers()
        )
        return self._handle_response(response)

    def delete_review(self, review_id: int) -> Dict[str, Any]:
        """Delete own review (authenticated users only)"""
        response = requests.delete(
            f"{self.base_url}/api/v1/review/{review_id}",
            headers=self._get_headers()
        )
        return self._handle_response(response)
