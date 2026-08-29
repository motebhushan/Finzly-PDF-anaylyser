import { useState } from 'react';
import usePdfAnalysis from '../hooks/usePdfAnalysis';
import UrlInputForm from '../components/UrlInputForm/UrlInputForm';
import FileUploadForm from '../components/FileUploadForm/FileUploadForm';
import AnalysisResult from '../components/AnalysisResult/AnalysisResult';
import LoadingSpinner from '../components/LoadingSpinner/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage/ErrorMessage';
import styles from './HomePage.module.css';

/**
 * HomePage — the single page of the application.
 *
 * Composes components and wires them to the usePdfAnalysis hook.
 * All state and API logic is in the hook; this page is purely compositional.
 */
const HomePage = () => {
  const { result, isLoading, error, handleAnalyse, handleUpload, reset } = usePdfAnalysis();
  const [activeTab, setActiveTab] = useState('url'); // 'url' or 'upload'

  return (
    <main className={styles.main} id="main-content">
      {/* Hero section */}
      <header className={styles.hero}>
        <div className={styles.badge}>Powered by Google Gemini</div>
        <h1 className={styles.title}>
          PDF <span className={styles.titleAccent}>Analyser</span>
        </h1>
        <p className={styles.subtitle}>
          Paste a publicly accessible PDF URL and get an instant AI-powered
          structured analysis — document type, title, authors, summary, and key insights.
        </p>
      </header>

      {/* Input card with Tabs */}
      <div className={styles.card}>
        <div className={styles.tabs}>
          <button
            className={`${styles.tabBtn} ${activeTab === 'url' ? styles.activeTab : ''}`}
            onClick={() => setActiveTab('url')}
            disabled={isLoading}
          >
            URL Input
          </button>
          <button
            className={`${styles.tabBtn} ${activeTab === 'upload' ? styles.activeTab : ''}`}
            onClick={() => setActiveTab('upload')}
            disabled={isLoading}
          >
            File Upload
          </button>
        </div>
        
        <div className={styles.tabContent}>
          {activeTab === 'url' ? (
            <UrlInputForm onSubmit={handleAnalyse} isLoading={isLoading} />
          ) : (
            <FileUploadForm onSubmit={handleUpload} isLoading={isLoading} />
          )}
        </div>
      </div>

      {/* Output section */}
      {isLoading && (
        <div className={styles.card}>
          <LoadingSpinner />
        </div>
      )}

      {error && !isLoading && (
        <div className={styles.card}>
          <ErrorMessage message={error} onRetry={reset} />
        </div>
      )}

      {result && !isLoading && (
        <div className={styles.card}>
          <AnalysisResult result={result} />
        </div>
      )}
    </main>
  );
};

export default HomePage;
