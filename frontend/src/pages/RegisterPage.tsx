import { FormEvent, useState } from 'react';
import { api } from '@/api/client';
import { useNavigate } from 'react-router-dom';

type Role = 'USER' | 'ADMIN';

export default function RegisterPage() {
    const [name, setName] = useState('Test User');
    const [email, setEmail] = useState('user@example.com');
    const [password, setPassword] = useState('password');
    const [role, setRole] = useState<Role>('USER');
    const [error, setError] = useState<string | null>(null);
    const [ok, setOk] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        setError(null);
        setOk(null);
        setLoading(true);
        try {
            await api.post('/auth/register', { name, email, password, role });
            setOk('Registered successfully. You can login now.');
            setTimeout(() => navigate('/login', { replace: true }), 800);
        } catch (e: any) {
            setError(e?.response?.data || e?.message || 'Register failed');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h2>Register</h2>
            {error && <p style={{ color: 'crimson' }}>{error}</p>}
            {ok && <p style={{ color: 'green' }}>{ok}</p>}
            <form onSubmit={onSubmit} className="grid" style={{ gap: 12, maxWidth: 360 }}>
                <div className="field"><input placeholder="name" value={name} onChange={(e) => setName(e.target.value)} /></div>
                <div className="field"><input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} /></div>
                <div className="field"><input placeholder="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></div>
                <div className="field"><select value={role} onChange={(e) => setRole(e.target.value as Role)}>
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                </select></div>
                <button className="btn" type="submit" disabled={loading}>{loading ? '...' : 'Register'}</button>
            </form>
        </div>
    );
}


