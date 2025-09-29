import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { FiBox, FiShoppingCart, FiLogIn, FiUserPlus, FiPlusSquare } from 'react-icons/fi';
import ProductsPage from './pages/ProductsPage';
import OrdersPage from './pages/OrdersPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AddProductPage from './pages/AddProductPage';

function App() {
    return (
        <div className="container" style={{ fontFamily: 'system-ui, -apple-system, Segoe UI, Roboto, Ubuntu, Cantarell, Noto Sans, sans-serif' }}>
            <nav className="navbar">
                <Link to="/products"><FiBox style={{ position: 'relative', top: 2 }} /> Products</Link>
                <Link to="/orders"><FiShoppingCart style={{ position: 'relative', top: 2 }} /> Orders</Link>
                <span className="spacer" />
                <Link to="/add-product"><FiPlusSquare style={{ position: 'relative', top: 2 }} /> Add Product</Link>
                <Link to="/login"><FiLogIn style={{ position: 'relative', top: 2 }} /> Login</Link>
                <Link to="/register"><FiUserPlus style={{ position: 'relative', top: 2 }} /> Register</Link>
                <button className="btn" onClick={() => { localStorage.removeItem('token'); location.reload(); }}>Logout</button>
            </nav>
            <Routes>
                <Route path="/" element={<Navigate to="/products" replace />} />
                <Route path="/products" element={<ProductsPage />} />
                <Route path="/orders" element={<OrdersPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/add-product" element={<AddProductPage />} />
            </Routes>
        </div>
    );
}

export default App;



