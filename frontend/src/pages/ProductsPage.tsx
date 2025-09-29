import { useEffect, useState } from 'react';
import { listProducts, type Product } from '@/api/client';

export default function ProductsPage() {
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        setLoading(true);
        listProducts()
            .then(setProducts)
            .catch((e) => setError(e?.message ?? 'Failed to load products'))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <p>Loading products...</p>;
    if (error) return <p style={{ color: 'crimson' }}>{error}</p>;

    return (
        <div>
            <h2 style={{ marginBottom: 12 }}>Products</h2>
            {products.length === 0 ? (
                <p className="muted">No products.</p>
            ) : (
                <ul className="grid products" style={{ padding: 0, listStyle: 'none' }}>
                    {products.map((p) => (
                        <li key={p.id} className="card">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <strong>{p.name}</strong>
                                    <div className="muted">{p.description}</div>
                                </div>
                                <div>${p.price?.toFixed?.(2) ?? p.price}</div>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}



