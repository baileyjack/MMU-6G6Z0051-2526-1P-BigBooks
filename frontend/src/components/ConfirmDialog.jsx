export default function ConfirmDialog({ title, message, onConfirm, onCancel }) {
    const handleConfirm = () => {
        const ok = window.confirm(message);
        if (!ok) return;
        onConfirm();
    };

    return (
        <div className="modal-backdrop">
            <div className="modal small">
                <h2>{title}</h2>
                <p>{message}</p>
                <div className="modal-actions">
                    <button className="secondary-btn" onClick={onCancel}>Cancel</button>
                    <button className="danger-btn" onClick={handleConfirm}>Confirm</button>
                </div>
            </div>
        </div>
    );
}
