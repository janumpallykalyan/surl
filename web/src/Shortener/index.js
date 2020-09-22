import React, { Component } from 'react';
import './Shortener.css';

import LinkForm from './LinkForm';
import MidInfoBar from './MidInfoBar';
import UrlList from './UrlList';
import axios from "axios";

export default class Shortener extends Component {

  constructor(props) {
    super(props);

    this.state = {
      shortenedLinkList: [],
      // shortenedLinkList: sessionStorage.getItem('shortenedLinkList') ? JSON.parse(sessionStorage.getItem('shortenedLinkList')) : [],
    };

    this.populateShortenedLinkList = this.populateShortenedLinkList.bind(this);
    this.clearShortenedLinkList = this.clearShortenedLinkList.bind(this);
    this.populateShortenedLinkList();
  }

  populateShortenedLinkList() {
    let id = localStorage.getItem('userId');
    if(id) {
      this.setState({userId: id});
      axios.get('/api/v1/users/' + id +'/surls')
          .then((response) => {
            this.setState({
              shortenedLinkList: response.data
            });
          })
          .catch((err) => {
          });
    }
  }

  clearShortenedLinkList() {
    console.log('Shortener, clearShortenedLinkList');
    // this.setState({
    //   shortenedLinkList: []
    // });
  }

  render() {
    return (
      <div className="shortener-main">
        <LinkForm populateLinkList={this.populateShortenedLinkList}/>
        <MidInfoBar clearLinkList={this.clearShortenedLinkList}/>
        <UrlList shortenedLinkList={this.state.shortenedLinkList}/>
      </div>
    );
  }
}
