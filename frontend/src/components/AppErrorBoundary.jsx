import { Component } from 'react';
import { FiRefreshCw, FiAlertTriangle } from 'react-icons/fi';

export default class AppErrorBoundary extends Component {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="app-error-screen" role="alert">
          <div className="app-error-card">
            <span className="app-error-icon"><FiAlertTriangle aria-hidden="true" /></span>
            <p className="eyebrow">ECHO WORKSPACE</p>
            <h1>That page didn’t load.</h1>
            <p>Please refresh and try again. Your account and saved requests are unchanged.</p>
            <button className="btn btn-echo" type="button" onClick={() => window.location.reload()}>
              <FiRefreshCw aria-hidden="true" /> Reload Echo
            </button>
          </div>
        </main>
      );
    }
    return this.props.children;
  }
}
