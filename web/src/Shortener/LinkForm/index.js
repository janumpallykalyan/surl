import React, { Component } from 'react';
import './LinkForm.css';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import axios from 'axios';
import uuid from 'uuid';

export default class LinkForm extends Component {

  constructor(props) {
    super(props);

    this.state = {
      doesPlaceholderAppear: true,
      userId: undefined
    }

    this.shortenLink = this.shortenLink.bind(this);
    this.onTextInputChange = this.onTextInputChange.bind(this);
  }

  componentDidMount() {
    let id = localStorage.getItem('userId');
    if(id) {
      this.setState({userId: id});
    }
  }

  shortenLink(event) {
    event.preventDefault();

    if(!this.state.userId) {
      let userId = uuid.v4();
      this.setState({userId: userId.toString()});
      localStorage.setItem('userId', userId);
    }

    let linkToShorten = this._linkToShorten.value;
    if (!this.state.doesPlaceholderAppear){
      // console.log('link shortened: ', linkToShorten, this.props);
      axios.post('/api/v1/shortify',
                { longUrl: linkToShorten, userId: this.state.userId })
        .then((response) => {
          toast("Short URL successfully generated!!");
          let linkData = {
            shortcode: response.data.code,
            visits: 1,
            lastVisited: Date.now(),
            originalLink: linkToShorten
          }
          this.props.populateLinkList(linkData);
        }
      )
          .catch((error) => {
            toast(error.message);
          });

      this._linkToShorten.value = '';
      this.onTextInputChange();
    }
    else {
      console.log('button disabled!');
    }
  }

  onTextInputChange() {
    // console.log('onTextInputChange: ', this._linkToShorten.value);
    if (this._linkToShorten.value.length > 0) {
      this.setState({
        doesPlaceholderAppear: false
      });
    }
    else {
      this.setState({
        doesPlaceholderAppear: true
      });
    }
  }

  render() {
    return (
      <div className="url-form">
        <form onSubmit={this.shortenLink}>
          <input className="url-input normal-text"
            ref={(a) => this._linkToShorten = a}
            onChange={() => this.onTextInputChange()}
            placeholder="Paste the link you want to shorten here">
          </input>
          <button className={(this.state.doesPlaceholderAppear ? 'shorten-button-disabled' : 'shorten-button') + " normal-text"}
            type="submit">
            Shorten this link
          </button>
        </form>
        <ToastContainer />
      </div>
    );
  }
}
