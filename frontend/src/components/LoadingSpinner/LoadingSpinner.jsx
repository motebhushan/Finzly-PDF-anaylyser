import styles from './LoadingSpinner.module.css';

/**
 * LoadingSpinner — shown while the LLM analysis is in-flight.
 * Communicates to the user that a potentially slow operation is happening.
 */
const LoadingSpinner = () => {
  return (
    <div
      id="loading-spinner"
      className={styles.container}
      role="status"
      aria-live="polite"
      aria-label="Analysing PDF, please wait…"
    >
      <div className={styles.orbitWrapper}>
        <div className={styles.orbit} />
        <div className={styles.core} />
      </div>
      <p className={styles.message}>Analysing your PDF…</p>
      <p className={styles.subMessage}>
        This may take a few seconds while the AI reads your document.
      </p>
    </div>
  );
};

export default LoadingSpinner;
