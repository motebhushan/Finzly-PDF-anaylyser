import styles from './ErrorMessage.module.css';

/**
 * ErrorMessage — displays user-friendly error messages with a retry affordance.
 *
 * Props:
 *   message: string   — the error message to display
 *   onRetry: function — called when user clicks "Try Again"
 */
const ErrorMessage = ({ message, onRetry }) => {
  return (
    <div
      id="error-message"
      className={styles.container}
      role="alert"
      aria-live="assertive"
    >
      <div className={styles.iconWrapper} aria-hidden="true">
        <span className={styles.icon}>⚠</span>
      </div>
      <div className={styles.content}>
        <h3 className={styles.title}>Analysis Failed</h3>
        <p className={styles.message}>{message}</p>
        {onRetry && (
          <button
            id="retry-button"
            className={styles.retryButton}
            onClick={onRetry}
          >
            Try Again
          </button>
        )}
      </div>
    </div>
  );
};

export default ErrorMessage;
