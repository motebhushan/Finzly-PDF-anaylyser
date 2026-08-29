import { useState } from 'react';
import styles from './UrlInputForm.module.css';

/**
 * UrlInputForm — controlled input for PDF URL + submit button.
 *
 * Props:
 *   onSubmit(url: string) — called when user submits a valid URL
 *   isLoading: boolean    — disables form during API call
 */
const UrlInputForm = ({ onSubmit, isLoading }) => {
  const [url, setUrl] = useState('');
  const [localError, setLocalError] = useState('');

  const validateUrl = (value) => {
    if (!value.trim()) return 'Please enter a PDF URL';
    if (!value.startsWith('http://') && !value.startsWith('https://')) {
      return 'URL must start with http:// or https://';
    }
    return '';
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const error = validateUrl(url);
    if (error) {
      setLocalError(error);
      return;
    }
    setLocalError('');
    onSubmit(url.trim());
  };

  const handleChange = (e) => {
    setUrl(e.target.value);
    if (localError) setLocalError(''); // Clear error as user types
  };

  return (
    <form
      id="pdf-analyse-form"
      className={styles.form}
      onSubmit={handleSubmit}
      noValidate
    >
      <div className={styles.inputGroup}>
        <label htmlFor="pdf-url-input" className={styles.label}>
          PDF Document URL
        </label>
        <div className={styles.inputRow}>
          <input
            id="pdf-url-input"
            type="url"
            className={`${styles.input} ${localError ? styles.inputError : ''}`}
            placeholder="https://arxiv.org/pdf/1706.03762"
            value={url}
            onChange={handleChange}
            disabled={isLoading}
            aria-describedby={localError ? 'url-error' : undefined}
          />
          <button
            id="analyse-button"
            type="submit"
            className={styles.button}
            disabled={isLoading || !url.trim()}
          >
            {isLoading ? (
              <span className={styles.buttonLoading}>
                <span className={styles.spinner} aria-hidden="true" />
                Analysing…
              </span>
            ) : (
              'Analyse PDF'
            )}
          </button>
        </div>
        {localError && (
          <p id="url-error" className={styles.errorText} role="alert">
            {localError}
          </p>
        )}
      </div>
    </form>
  );
};

export default UrlInputForm;
