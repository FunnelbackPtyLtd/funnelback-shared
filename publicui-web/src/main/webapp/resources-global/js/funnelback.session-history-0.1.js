/*
 * Funnelback History widget to help display serach and click histories
 * version 0.1.0
 *
 * author: Liliana Nowak
 * Copyright Funnelback, 2019
 * 
 * @usage
 * initiation: var myHistory = new Funnelback.SessionHistory({ collection: 'my-collection-name' });
 * open history box: myHistory.show();
 * hide history box: myHistory.hide();
 * toggle visibility of history box: myHistory.toggle();
 * get all settings of history box: myHistory.getOption();
 * clear all click history data: myHistory.clearClicks();
 * clear all search history data: myHistory.clearSearches();
 */
if (!window.Funnelback) window.Funnelback = {}; // create namespace

window.Funnelback.SessionHistory = (function() {
  'use strict'

  var Constructor = function(options) {
    return this.init(options);
  }

  // Default options
  Constructor.defaults = {
    apiBase: '/', // web service URL to get cart data from
    collection: null, // collection name; required parameter

    // Selectors to DOM elements displaying content; each CSS selector should return not more than one element
    historySelector: '#search-history', // CSS selector to element where content of history should be displayed
    clickEmptySelector: '.session-history-click-empty', // CSS selector to element displaying message about no click history data
    clickResultsSelector: '.session-history-click-results', // CSS selector to element displaying click history data
    searchEmptySelector: '.session-history-search-empty', // CSS selector to element displaying message about no search history data
    searchResultsSelector: '.session-history-search-results', // CSS selector to element displaying search history data
    pageSelector: ['#search-results-content', '#search-cart'], // list of CSS selectors to parts of page to hide it when history is displayed

    // Selectors to DOM elements triggering events; each CSS selector can return zero or more elements
    clearClickSelector: '.session-history-clear-click', // CSS selector to element on clicking which click history data will be cleared
    clearSearchSelector: '.session-history-clear-search', // CSS selector to element on clicking which search history data will be cleared
    hideSelector: '.session-history-hide', // CSS selector to element on clicking which history box will be hidden
    showSelector: '.session-history-show', // CSS selector to element on clicking which history box will be shown
    toggleSelector: '.session-history-toggle', // CSS selector to element on clicking which history box will be toggled
  };

  /**
   * Initialise history widget with provided options
   */
  Constructor.prototype.init = function(options) {
    if (!options.collection) {
      console.error('Missing "collection" parameter');
      return null;
    }
    Constructor.options = Utils.extend(Constructor.defaults, options || {});
    if (!Constructor.options.pageSelector) Constructor.options.pageSelector = [];
    View.init(Constructor.options);
    Constructor.prototype.hide();
    return this;
  };

  /**
   * Destroy history widget, erase options
   */
  Constructor.prototype.destroy = function() {
    Constructor.options  = {};
    return null;
  };

  /**
   * Clear all click history for current user
   */
  Constructor.prototype.clearClicks = function() {
    View.clear(this.getOption(), 'click');
    return this;
  }

  /**
   * Clear all search history for current user
   */
  Constructor.prototype.clearSearches = function() {
    View.clear(this.getOption(), 'search');
    return this;
  }

  /**
   * Get history widget settings
   * - no arguments - get all settings
   * - one argument `key` - get value of specific setting for that argument
   */
  Constructor.prototype.getOption = function(key, val) {
    if (arguments.length === 0) {
      return Constructor.options ; // get all options
    }
    if (typeof key === 'string') {
      return Constructor.options [key]; // get value of specific option
    }
  };

  /**
   * Open history box
   */
  Constructor.prototype.show = function() {
    if (View.history) View.history.style.display = 'block';
    View.toggle(View.page, 'none');
    View.isHidden = false;
    return this;
  };

  /**
   * Close history box
   */
  Constructor.prototype.hide = function() {
    if (View.history) View.history.style.display = 'none';
    View.toggle(View.page, 'block');
    View.isHidden = true;
    return this;
  };

  /**
   * Toggle history box
   */
  Constructor.prototype.toggle = function() {
    return View.isHidden ? Constructor.prototype.show() : Constructor.prototype.hide();
  };

  const Api = {
    clickUrl: 's/click-history.json',
    searchUrl: 's/search-history.json',

    delete: function(options, type) {
      return Api.request('delete', options, type);
    },

    request: function(method, options, type) {
      return new Promise(function(resolve, reject) {
        const xhr = new XMLHttpRequest(), url = Api.getUrl(options, type);
        // Setup callbacks
        xhr.onload = function() {
          if (this.status !== 200) { // If the request failed
            reject({url: url, error: this});
          } else { // If the request succeeded
            try {
              const data = JSON.parse(this.responseText);
              resolve({url: url, data: data});
            } catch(error) {
              reject({url: url, error: error});
            }
          }
        }

        xhr.onerror = function() {
          reject({url: url, error: xhr});
        }

        xhr.open(method, url, true);
        xhr.setRequestHeader('Accept', 'application/json');
        xhr.setRequestHeader('Content-Type', 'text/plain');
        xhr.send();
      });
    },

    getParamsString: function(params) {
      const str = [];
      for (var p in params) {
        if (params.hasOwnProperty(p)) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(params[p]));
      }
      return str.join('&');
    },

    getUrl: function(options, type) {
      return options.apiBase + Api[type + 'Url'] + '?' + Api.getParamsString({collection: options.collection});
    }
  };

  const View = {
    // Store references to DOM elements
    history: null,
    page: [],
    clickClear: null,
    clickEmpty: null,
    clickResults: null,
    searchClear: null,
    searchEmpty: null,
    searchResults: null,
    // Store state if history box is hidden or shown
    isHidden: true,

    init: function(options) {
      View.setElement(options.historySelector, 'history', 'warn'); // Find element displaying history data
      View.setElement(options.clickEmptySelector, 'clickEmpty'); // Find element displaying click history empty
      View.setElement(options.clickResultsSelector, 'clickResults'); // Find element displaying click history data
      View.setElement(options.searchEmptySelector, 'searchEmpty'); // Find element displaying search history empty
      View.setElement(options.searchResultsSelector, 'searchResults'); // Find element displaying search history data

      View.setListeners(options.showSelector, Constructor.prototype.show);
      View.setListeners(options.hideSelector, Constructor.prototype.hide);
      View.setListeners(options.toggleSelector, Constructor.prototype.toggle);
      View.setListeners(options.clearClickSelector, Constructor.prototype.clearClicks, 'clickClear');
      View.setListeners(options.clearSearchSelector, Constructor.prototype.clearSearches, 'searchClear');

      // Toggle display of history resulst and no-data message
      View.clickResults ? View.results('click') : View.noResults('click');
      View.searchResults ? View.results('search') : View.noResults('search');

      // Find elements to be hidden when history box is displayed
      for (var i = 0, len = options.pageSelector.length; i < len; i++) {
        const el = document.querySelector(options.pageSelector[i]);
        if (el) View.page.push(el);
        else console.warn('No element was found for ' + options.pageSelector[i] + ' selector');
      }
    },

    /**
     * Call API to clear specified history data for current user
     * - widget options
     * - type of history data: `search` or `click`
     */
    clear: function(options, type) {
      if (!confirm('Your ' + type + ' history will be cleared')) return;
      Api.delete(options, type).then(function(response) {
        View.noResults(type);
      }).catch(function(error) {
        console.error('Something went wrong and ' + type + ' history was not cleared. Please try again later...', error);
      });
    },

    /**
     * Find first DOM element matching selector
     * - CSS selector
     * - name of field to store DOM element reference
     * - name of level to log message about not-found element in console
     */
    setElement: function(selector, name, level) {
      View[name] = document.querySelector(selector);
      if (!View[name]) console[level ? level : 'log']('No element was found for ' + selector + ' selector');
    },

    /**
     * Add event listener to all DOM elements matching selector
     * - CSS selector
     * - event to execute on DOM element click
     * - name of field to store DOM element reference
     */
    setListeners: function(selector, listener, name) {
      const elements = document.querySelectorAll(selector);
      for (var i = 0, len = elements.length; i < len; i++) elements[i].addEventListener('click', listener.bind(Constructor.prototype));
      if (name) View[name] = elements;
    },

    /**
     * Hide history results and clear data button, display no data message 
     * - type of history box: `search` or `click`
     */
    noResults: function(type) {
      View.toggle(View[type + 'Clear'], 'none');
      if (View[type + 'Empty']) View.toggle([View[type + 'Empty']], 'block');
      if (View[type + 'Results']) View.toggle([View[type + 'Results']], 'none');
    },

    /**
     * Hide no data message and display history results
     * - type of history box: `search` or `click`
     */
    results: function(type) {
      if (View[type + 'Empty']) View.toggle([View[type + 'Empty']], 'none');
      if (View[type + 'Results']) View.toggle([View[type + 'Results']], 'block');
    },

    /**
     * Show or hide DOM elements in the page
     * - value of CSS property 'display'
     */
    toggle: function(elements, display) {
      for (var i = 0, len = elements.length; i < len; i++) elements[i].style.display = display;
    }
  };

  const Utils = {
    // Deep extend of one object with properites of other object
    extend: function(obj, src) {
      for (var key in src) {
        if (src.hasOwnProperty(key)) obj[key] = typeof src[key] === 'object' ? Utils.extend(obj[key] || {}, src[key]) : src[key];
      }
      return obj;
    }
  };

  return Constructor;
}());