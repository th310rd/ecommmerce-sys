import { FormEvent, useEffect, useMemo, useState } from 'react';
import { createOrder, listOrders, listProducts, type Order, type OrderItemInput, type Product } from '@/api/client';

export default function OrdersPage() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [products, setProducts] = useState<Product[]>([]);
    const [selectedItems, setSelectedItems] = useState<Record<string, number>>({});
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState<boolean>(false);

    useEffect(() => {
        setLoading(true);
        Promise.all([listOrders(), listProducts()])
            .then(([ordersRes, productsRes]) => {
                setOrders(ordersRes);
                setProducts(productsRes);
            })
            .catch((e) => setError(e?.message ?? 'Failed to load data'))
            .finally(() => setLoading(false));
    }, []);

    const total = useMemo(() => {
        return Object.entries(selectedItems).reduce((sum, [productId, qty]) => {
            const product = products.find((p) => p.id === productId);
            if (!product) return sum;
            return sum + (product.price || 0) * qty;
        }, 0);
    }, [selectedItems, products]);

    function onQuantityChange(productId: number, qty: number) {
        setSelectedItems((prev) => ({ ...prev, [productId]: qty }));
    }

    async function onSubmit(e: FormEvent) {
        e.preventDefault();
        const items: OrderItemInput[] = Object.entries(selectedItems)
            .filter(([, qty]) => qty > 0)
            .map(([productId, quantity]) => ({ productId: Number(productId), quantity }));
        if (items.length === 0) return;
        setSubmitting(true);
        setError(null);
        try {
            const created = await createOrder(items);
            setOrders((prev) => [created, ...prev]);
            setSelectedItems({});
        } catch (e: any) {
            setError(e?.message ?? 'Failed to create order');
        } finally {
            setSubmitting(false);
        }
    }

    if (loading) return <p className="muted">Loading...</p>;
    if (error) return <p style={{ color: 'crimson' }}>{error}</p>;

    return (
        <div>
            <h2>Create Order</h2>
            <form onSubmit={onSubmit} className="grid" style={{ gap: 12 }}>
                <div style={{ display: 'grid', gap: 8 }}>
                    {products.map((p) => (
                        <label key={p.id} className="card" style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                            <span style={{ minWidth: 200 }}>{p.name}</span>
                            <input
                                type="number"
                                min={0}
                                step={1}
                                value={selectedItems[p.id] ?? 0}
                                onChange={(e) => onQuantityChange(p.id, Number(e.target.value))}
                                style={{ width: 90 }}
                            />
                            <span className="spacer" />
                            <span>${p.price?.toFixed?.(2) ?? p.price}</span>
                        </label>
                    ))}
                </div>
                <div className="card" style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <strong>Total:</strong> ${total.toFixed(2)}
                    <span className="spacer" />
                    <button className="btn" type="submit" disabled={submitting || total === 0}>Create Order</button>
                </div>
            </form>

            <h2 style={{ marginTop: 24 }}>Orders</h2>
            {orders.length === 0 ? (
                <p className="muted">No orders.</p>
            ) : (
                <ul className="grid" style={{ listStyle: 'none', padding: 0 }}>
                    {orders.map((o) => (
                        <li key={o.id} className="card">
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <strong>Order #{o.id}</strong>
                                <span>{o.status}</span>
                            </div>
                            <ul>
                                {o.items.map((it, idx) => (
                                    <li key={idx}>
                                        {it.productId} × {it.quantity}
                                    </li>
                                ))}
                            </ul>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}



