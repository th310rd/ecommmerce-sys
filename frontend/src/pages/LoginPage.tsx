import { FormEvent, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '@/api/client';

export default function LoginPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('user@example.com');
    const [password, setPassword] = useState('password');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        setError(null);
        setLoading(true);
        try {
            const res = await api.post('/auth/login', { email, password });
            const token = res.data as string;
            localStorage.setItem('token', token);
            navigate('/products', { replace: true });
        } catch (e: any) {
            setError(e?.response?.data || e?.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h2>Login</h2>
            {error && <p style={{ color: 'crimson' }}>{error}</p>}
            <form onSubmit={onSubmit} className="grid" style={{ gap: 12, maxWidth: 320 }}>
                <div className="field"><input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} /></div>
                <div className="field"><input placeholder="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></div>
                <button className="btn" type="submit" disabled={loading}>{loading ? '...' : 'Login'}</button>
            </form>
            <p>Don't have an account? <Link to="/register">Register</Link></p>
        </div>
    );
}


