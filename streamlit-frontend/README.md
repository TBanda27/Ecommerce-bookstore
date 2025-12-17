# Books E-commerce Store - Streamlit Frontend

A modern, fully-featured Streamlit frontend for the Books E-commerce microservices platform.

## Features

- **User Authentication**:
  - Traditional login with email/password
  - Google OAuth2 social login
  - Email verification
  - JWT-based session management
- **Book Browsing**: Search and filter books by category
- **Reviews System**: Read and write book reviews
- **User Profile**: Manage account settings
- **Admin Panel**: Comprehensive management interface for books, categories, prices, inventory, and users
- **Real-time Inventory**: Check stock availability
- **Responsive Design**: Clean and intuitive interface

## Technology Stack

- **Frontend Framework**: Streamlit (Python)
- **Backend API**: Spring Boot microservices
- **Authentication**: JWT-based security + Google OAuth2
- **HTTP Client**: Requests library
- **Deployment**: Docker, Streamlit Community Cloud

## Project Structure

```
streamlit-frontend/
├── app.py                      # Main home page
├── pages/                      # Multi-page app
│   ├── 1_📖_Book_Details.py   # Book details view
│   ├── 2_📝_Register.py       # User registration
│   ├── 3_👤_Profile.py        # User profile
│   ├── 4_⭐_My_Reviews.py     # User's reviews
│   ├── 5_🛠️_Admin_Panel.py   # Admin dashboard
│   └── 6_🔐_OAuth2_Callback.py # OAuth2 redirect handler
├── utils/                      # Utility modules
│   ├── api_client.py          # API Gateway client
│   ├── auth.py                # Authentication helpers
│   └── helpers.py             # General utilities
├── components/                 # Reusable components
│   └── sidebar.py             # Navigation sidebar
├── .streamlit/                # Streamlit configuration
│   └── config.toml
├── requirements.txt           # Python dependencies
├── Dockerfile                 # Docker configuration
├── docker-compose.yml         # Docker Compose setup
└── README.md                  # This file
```

## Prerequisites

- Python 3.11 or higher
- Running Books E-commerce backend services (API Gateway on port 9090)
- (Optional) Docker for containerized deployment

## Installation & Setup

### Option 1: Local Development

1. **Clone the repository**:
   ```bash
   cd "D:\Spring Boot\Books-Ecommerce Project\streamlit-frontend"
   ```

2. **Create a virtual environment**:
   ```bash
   python -m venv venv

   # Windows
   venv\Scripts\activate

   # Linux/Mac
   source venv/bin/activate
   ```

3. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure environment variables**:
   ```bash
   # Copy the example file
   cp .env.example .env

   # Edit .env and set your API Gateway URL
   # Default: API_GATEWAY_URL=http://localhost:9090
   ```

5. **Run the application**:
   ```bash
   streamlit run app.py
   ```

6. **Access the app**:
   Open your browser and navigate to: `http://localhost:8501`

### Option 2: Docker Deployment

1. **Build the Docker image**:
   ```bash
   docker build -t books-streamlit-frontend .
   ```

2. **Run the container**:
   ```bash
   docker run -p 8501:8501 \
     -e API_GATEWAY_URL=http://host.docker.internal:9090 \
     books-streamlit-frontend
   ```

3. **Or use Docker Compose**:
   ```bash
   docker-compose up -d
   ```

### Option 3: Streamlit Community Cloud

1. **Push your code to GitHub**:
   ```bash
   git add .
   git commit -m "Add Streamlit frontend"
   git push origin main
   ```

2. **Deploy to Streamlit Cloud**:
   - Go to [share.streamlit.io](https://share.streamlit.io)
   - Click "New app"
   - Connect your GitHub repository
   - Select `streamlit-frontend/app.py` as the main file
   - Set environment variable: `API_GATEWAY_URL=<your-backend-url>`
   - Click "Deploy"

## Configuration

### Environment Variables

Create a `.env` file in the `streamlit-frontend` directory:

```env
API_GATEWAY_URL=http://localhost:9090
```

For production deployment, update this to your actual API Gateway URL.

### Streamlit Configuration

Edit `.streamlit/config.toml` to customize:

- Theme colors
- Server settings
- Browser behavior

## Usage

### For Regular Users

1. **Register an Account**:
   - Navigate to the Register page
   - Fill in your details
   - Check your email for verification link
   - Verify your account

2. **Login**:
   - Use the sidebar login form
   - Enter your email and password

3. **Browse Books**:
   - View all available books
   - Filter by category
   - Search by title or author
   - View book details and reviews

4. **Write Reviews**:
   - Click on any book
   - Scroll to the review section
   - Rate and write your review

5. **Manage Profile**:
   - Update your username and email
   - View your account details

6. **View Your Reviews**:
   - Access "My Reviews" page
   - Edit or delete your reviews

### For Administrators

1. **Access Admin Panel**:
   - Login with an admin account
   - Navigate to Admin Panel

2. **Manage Books**:
   - Add new books
   - Edit existing books
   - Delete books

3. **Manage Categories**:
   - Create book categories
   - Delete categories

4. **Manage Prices**:
   - Set prices for books
   - Update existing prices
   - Delete prices

5. **Manage Inventory**:
   - Add inventory records
   - Update stock quantities
   - Track warehouse locations

6. **Manage Users**:
   - View all registered users
   - Delete user accounts
   - Check verification status

## API Integration

The frontend communicates with the following backend endpoints via the API Gateway (`http://localhost:9090`):

### Authentication Endpoints
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/user` - User registration
- `GET /api/v1/auth/verify` - Email verification
- `GET /api/v1/user/me` - Get current user

### Books Endpoints
- `GET /api/v1/books` - Get all books (with pagination)
- `GET /api/v1/books/{id}` - Get book by ID
- `POST /api/v1/books` - Create book (admin)
- `PUT /api/v1/books/{id}` - Update book (admin)
- `DELETE /api/v1/books/{id}` - Delete book (admin)

### Category Endpoints
- `GET /api/v1/category` - Get all categories
- `POST /api/v1/category` - Create category (admin)
- `DELETE /api/v1/category/{id}` - Delete category (admin)

### Price Endpoints
- `GET /api/v1/price/book/{bookId}` - Get price for book
- `POST /api/v1/price` - Create price (admin)
- `DELETE /api/v1/price/book/{bookId}` - Delete price (admin)

### Inventory Endpoints
- `GET /api/v1/inventory/book/{bookId}` - Get inventory for book
- `POST /api/v1/inventory` - Create inventory (admin)
- `DELETE /api/v1/inventory/book/{bookId}` - Delete inventory (admin)

### Review Endpoints
- `GET /api/v1/review/book/{bookId}` - Get reviews for book
- `GET /api/v1/review/me` - Get current user's reviews
- `POST /api/v1/review` - Create review (authenticated)
- `PUT /api/v1/review/{id}` - Update review (authenticated)
- `DELETE /api/v1/review/{id}` - Delete review (authenticated)

## Deployment Options

### 1. Local Machine
- Simplest option for development
- Run with `streamlit run app.py`

### 2. Streamlit Community Cloud (Free)
- Best for demos and prototypes
- Automatic deployment from GitHub
- Limited resources on free tier
- Backend must be publicly accessible

### 3. Docker Container
- Consistent deployment across environments
- Easy to scale
- Can be deployed to any cloud provider

### 4. Cloud Platforms

#### AWS Deployment
```bash
# Use ECS Fargate or EC2 with Docker
# Or use Elastic Beanstalk for simplified deployment
```

#### Google Cloud Platform
```bash
# Deploy to Cloud Run
gcloud run deploy books-frontend \
  --source . \
  --platform managed \
  --region us-central1 \
  --set-env-vars API_GATEWAY_URL=<your-api-url>
```

#### Azure
```bash
# Deploy to Azure Container Instances or App Service
az container create \
  --resource-group books-rg \
  --name books-frontend \
  --image books-streamlit-frontend \
  --ports 8501 \
  --environment-variables API_GATEWAY_URL=<your-api-url>
```

#### Heroku
```bash
# Create Procfile:
# web: streamlit run app.py --server.port=$PORT

heroku create books-ecommerce-frontend
git push heroku main
```

#### Railway / Render
- Push to GitHub
- Connect repository
- Auto-deploy on push
- Set environment variables in dashboard

### 5. Self-Hosted VPS
```bash
# On DigitalOcean, Linode, Vultr, etc.
ssh user@your-vps

# Install dependencies
sudo apt update
sudo apt install python3-pip nginx

# Clone and setup
git clone <your-repo>
cd streamlit-frontend
pip install -r requirements.txt

# Run with systemd or supervisor for production
# Use Nginx as reverse proxy
```

## Production Considerations

### Security
- Always use HTTPS in production
- Set strong JWT secrets
- Enable CORS only for trusted domains
- Regular security updates

### Performance
- Use caching where appropriate
- Optimize image sizes
- Enable Streamlit's caching decorators
- Consider CDN for static assets

### Monitoring
- Set up health checks
- Monitor API response times
- Track user sessions
- Log errors and exceptions

### Backup
- Regular database backups
- Version control for code
- Document configuration

## Troubleshooting

### Connection Issues
```
Error: Error loading books: Connection refused
```
**Solution**: Ensure the backend API Gateway is running on port 9090

### Authentication Issues
```
Session expired. Please login again.
```
**Solution**: JWT token has expired. Login again to get a new token.

### CORS Errors
```
Access to fetch has been blocked by CORS policy
```
**Solution**: Check API Gateway CORS configuration to allow your frontend URL

### Module Not Found
```
ModuleNotFoundError: No module named 'streamlit'
```
**Solution**: Install dependencies with `pip install -r requirements.txt`

## Development

### Adding New Features

1. Create a new page in `pages/` directory
2. Add utility functions in `utils/`
3. Update sidebar navigation in `components/sidebar.py`
4. Add new API methods in `utils/api_client.py`

### Code Style
- Follow PEP 8 guidelines
- Use descriptive variable names
- Add docstrings to functions
- Keep functions focused and small

### Testing
- Test with both user and admin accounts
- Verify error handling
- Check pagination on large datasets
- Test on different screen sizes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is part of the Books E-commerce microservices platform.

## Support

For issues and questions:
- Check the troubleshooting section
- Review backend API documentation
- Check Streamlit documentation: https://docs.streamlit.io

## Acknowledgments

- Built with Streamlit
- Backend powered by Spring Boot microservices
- Icons and emojis for enhanced UX
