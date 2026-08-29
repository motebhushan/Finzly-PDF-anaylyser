import styles from './AnalysisResult.module.css';

const FIELDS = [
  { key: 'documentType', label: 'Document Type', icon: '📄' },
  { key: 'title',        label: 'Title',          icon: '📌' },
  { key: 'authors',      label: 'Authors',         icon: '✍️' },
  { key: 'summary',      label: 'Summary',         icon: '📝' },
  { key: 'keyTakeaway',  label: 'Key Takeaway',    icon: '💡' },
];

/**
 * AnalysisResult — renders the structured PDF analysis in a card grid.
 *
 * Props:
 *   result: { documentType, title, authors, summary, keyTakeaway }
 */
const AnalysisResult = ({ result }) => {
  return (
    <section
      id="analysis-result"
      className={styles.container}
      aria-label="PDF Analysis Result"
    >
      <h2 className={styles.heading}>Analysis Result</h2>
      <div className={styles.grid}>
        {FIELDS.map(({ key, label, icon }) => (
          <article
            key={key}
            id={`result-${key}`}
            className={`${styles.card} ${key === 'summary' || key === 'keyTakeaway' ? styles.cardWide : ''}`}
          >
            <div className={styles.cardHeader}>
              <span className={styles.icon} aria-hidden="true">{icon}</span>
              <span className={styles.cardLabel}>{label}</span>
            </div>
            <p className={styles.cardValue}>{result[key] || '—'}</p>
          </article>
        ))}
      </div>
    </section>
  );
};

export default AnalysisResult;
