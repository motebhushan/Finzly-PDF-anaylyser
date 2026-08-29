import React, { useState, useRef } from 'react';
import styles from './FileUploadForm.module.css';

const FileUploadForm = ({ onSubmit, isLoading }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file && file.type === 'application/pdf') {
      setSelectedFile(file);
    } else {
      alert('Please select a valid PDF file.');
      setSelectedFile(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (selectedFile) {
      onSubmit(selectedFile);
    }
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <div className={styles.inputGroup}>
        <label htmlFor="file-upload" className={styles.label}>
          Upload PDF File
        </label>
        <div className={styles.uploadArea}>
          <input
            type="file"
            id="file-upload"
            accept="application/pdf"
            className={styles.fileInput}
            onChange={handleFileChange}
            ref={fileInputRef}
            disabled={isLoading}
          />
          <div className={styles.uploadText}>
            {selectedFile ? (
              <span className={styles.fileName}>{selectedFile.name}</span>
            ) : (
              <span>Click to select or drag and drop a PDF file here</span>
            )}
          </div>
        </div>
      </div>
      <button
        type="submit"
        className={styles.submitBtn}
        disabled={isLoading || !selectedFile}
      >
        {isLoading ? 'Analysing...' : 'Analyse Uploaded PDF'}
      </button>
    </form>
  );
};

export default FileUploadForm;
