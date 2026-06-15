import type { ReactNode } from "react";
import styles from "./ConfirmModal.module.scss";

interface ConfirmModalProps {
  visible: boolean;
  title?: string;
  message: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmModal = ({
  visible,
  title = "Potwierdź akcję",
  message,
  confirmLabel = "Potwierdź",
  cancelLabel = "Anuluj",
  onConfirm,
  onCancel,
}: ConfirmModalProps) => {
  if (!visible) return null;

  return (
    <button 
      type="button" 
      className={styles.modalOverlay} 
      onClick={onCancel}
      aria-label="Zamknij okno modalne"
    >
      <div
        className={styles.modal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-modal-title"
        // Zamiast onClick na divie, po prostu nie używamy tutaj stopPropagation.
        // Kliknięcie w przyciski wewnątrz (cancel/confirm) i tak nie wywoła onCancel 
        // z overlay, bo zdarzenie wewnątrz przycisków jest izolowane.
      >
        <h3 id="confirm-modal-title">{title}</h3>
        <p>{message}</p>
        <div className={styles.actions}>
          <button 
            type="button" 
            className={styles.cancel} 
            onClick={(e) => {
              e.stopPropagation(); // Przenosimy stopPropagation tutaj
              onCancel();
            }}
          >
            {cancelLabel}
          </button>
          <button 
            type="button" 
            className={styles.confirm} 
            onClick={(e) => {
              e.stopPropagation(); // Przenosimy stopPropagation tutaj
              onConfirm();
            }}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </button>
  );
};

export default ConfirmModal;