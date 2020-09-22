import React, { Component } from 'react';
import './Header.css';

export default class Header extends Component {
  render() {
    return (
      <header className="app-header">
        <div className="logo app-title">
          SURL
        </div>
        <div className="normal-text app-subtitle">
            Shorten URL generator and analytics Project
        </div>
      </header>
    );
  }
}
