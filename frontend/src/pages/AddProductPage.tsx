import { FormEvent, useState } from 'react';
import { api } from '@/api/client';

export default function AddProductPage() {
    const [name, setName] = useState('Sample');
    const [description, setDescription] = useState('Test product');
    const [price, setPrice] = useState<number>(9.99);
    const [stockQuantity, setStockQuantity] = useState<number>(100);
    const [category, setCategory] = useState('GENERAL');
    const [imageUrl, setImageUrl] = useState('');
    const [rating, setRating] = useState('GOOD');
    const [ok, setOk] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        setOk(null);
        setError(null);
        setLoading(true);
        try {
            await api.post('/products', { name, description, price, stockQuantity, category, imageUrl, rating });
            setOk('Product created. Go to Products to see it.');
        } catch (e: any) {
            setError(e?.response?.data || e?.message || 'Create failed');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h2>Add Product</h2>
            {ok && <p style={{ color: 'green' }}>{ok}</p>}
            {error && <p style={{ color: 'crimson' }}>{error}</p>}
            <form onSubmit={onSubmit} className="grid" style={{ gap: 8, maxWidth: 420 }}>
                <div className="field"><input placeholder="name" value={name} onChange={(e) => setName(e.target.value)} /></div>
                <div className="field"><input placeholder="description" value={description} onChange={(e) => setDescription(e.target.value)} /></div>
                <div className="field"><input placeholder="price" type="number" step={0.01} value={price} onChange={(e) => setPrice(Number(e.target.value))} /></div>
                <div className="field"><input placeholder="stockQuantity" type="number" value={stockQuantity} onChange={(e) => setStockQuantity(Number(e.target.value))} /></div>
                <div className="field"><input placeholder="category" value={category} onChange={(e) => setCategory(e.target.value)} /></div>
                <div className="field"><input placeholder="imageUrl" value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} /></div>
                <div className="field"><select value={rating} onChange={(e) => setRating(e.target.value)}>
                    <option value="GOOD">GOOD</option>
                    <option value="AVERAGE">AVERAGE</option>
                    <option value="EXCELLENT">EXCELLENT</option>
                </select></div>
                <button className="btn" type="submit" disabled={loading}>{loading ? '...' : 'Create'}</button>
            </form>
        </div>
    );
}


