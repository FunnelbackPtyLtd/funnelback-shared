/*
 * Funnelback History widget to help display serach and click histories
 * version 0.1.0
 *
 * author: Liliana Nowak
 * Copyright Funnelback, 2019
 *
 * @requires Handlebars http://handlebarsjs.com@4.1.2
 *
 * @usage
 * initiation: var myHistory = new SearchHistory({ collection: 'my-collection-name' });
 * open history box: myHistory.show();
 * hide history box: myHistory.hide();
 * toggle visibility of history box: myHistory.toggle();
 * get all settings of history box: myHistory.getOption();
 * clear all search history data: myHistory.clear(myHistory.getOption(), 'search-history');
 * clear all clicks history data: myHistory.clear(myHistory.getOption(), 'click-history');
 */
var SearchHistory = (function() {
  'use strict'

  var Constructor = function(options) {
    return this.init(options);
  }

  // Default options
  Constructor.defaults = {
    apiBase: '/', // web service URL to get cart data from
    collection: null, // collection name; required parameter
    iconPrefix: 'glyphicon glyphicon-', // CSS class(es) prefix used to display icons
    box: {
      selector: '#search-history', // CSS selector to element where content of history should be displayed
      pageSelector: ['#search-results-display', '#search-cart'], // list of CSS selectors to parts of page to hide it when history is displayed
      icon: 'time', // icon to display in history box for main header; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      label: 'History', // label to display in history box for main header
      backIcon: 'arrow-left', // icon to display in history box for link to return to result page; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      backLabel: 'Back to results', // label to display in history box for link to return to result page
      clearClasses: 'btn btn-xs btn-danger', // CSS classes added to link element displayed in history box to clear all search/click history data
      clearIcon: 'remove', // icon to display in history box for link to clear all search/click history data
      clearLabel: 'Clear', // label to display in history box for link to clear all search/click history data
    },
    clickBox: {
      icon: 'heart', // icon to display for click history header; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      label: 'Recently clicked results', // label to display as click history header
      results: '#click-history-results', // CSS selector to list of click history results
    },
    searchBox: {
      icon: 'search', // icon to display for search history header; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      label: 'Recent searches', // label to display as search history header
      results: '#search-history-results', // CSS selector to list of search history results
    },
    trigger: {
      selector: '.flb-history-trigger', // CSS selector to element which should trigger display of history
      icon: null, // icon to display for search history trigger; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      isLabel: true, // [true|false]; if true label will be displayed as text of element else it will be displayed as `title` attribute of history trigger
      label: 'History', // label of history trigger, 
      template: '{{>icon-block}} {{>label-block}}' // Handlebars template used to display history box trigger
    },
  }

  /**
   * Create isolated Handlebars environment
   */
  Constructor.prototype.Handlebars = Handlebars.create();

  /**
   * Initialise history widget with provided options
   */
  Constructor.prototype.init = function(options) {
    if (!options.collection) {
      Log.warn('Missing "collection" parameter');
      return null;
    }

    this.options = Utils.extend(Constructor.defaults, options || {});
    if (!this.options.box.pageSelector) this.options.box.pageSelector = [];

    View.init(this.options);
    return this;
  };

  /**
   * Clear all items from choosen history
   * - history options
   * - type of history to clear: `search-history` or `click-history`
   */
  Constructor.prototype.clear = function(options, type) {
    const label = View.labelType(type);
    if (confirm('Your ' + label + ' will be cleared') === true) {
      Api.delete(options, type).then(function(response) {
        View.clear(options, type);
      }).catch(function(error) {
        Log.error('Something went wrong and ' + label + ' was not cleared. Please try again later...', error);
      });
    }
    return this;
  };

  /**
   * Destroy history widget, erase options
   */
  Constructor.prototype.destroy = function() {
    this.options  = {};
    return null;
  };

  /**
   * Open history box
   */
  Constructor.prototype.show = function() {
    View.element.style.display = 'block';
    View.togglePageElements('none');
    View.isHidden = false;
    return this;
  };

  /**
   * Close history box
   */
  Constructor.prototype.hide = function() {
    View.element.style.display = 'none';
    View.togglePageElements('block');
    View.isHidden = true;
    return this;
  };

  /**
   * Toggle history box
   */
  Constructor.prototype.toggle = function() {
    return View.isHidden ? Constructor.prototype.show() : Constructor.prototype.hide();
  };

  /**
   * Get history box settings
   * - no arguments - get all settings
   * - one argument `key` - get value of specific setting for that argument
   */
  Constructor.prototype.getOption = function(key, val) {
    if (arguments.length === 0) {
      return this.options; // get all options
    }

    if (typeof key === 'string') {
      return this.options[key]; // get value of specific option
    }
  };

  const Api = {
    clickUrl: 's/click-history.json',
    searchUrl: 's/search-history.json',

    delete: function(options, type, params) {
      return Api.request('delete', options, type, params);
    },

    request: function(method, options, type, params) {
      return new Promise(function(resolve, reject) {
        const xhr = new XMLHttpRequest(), url = Api.getUrl(options, type, params);
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
        xhr.send(params || {});
      });
    },

    getUrl: function(options, type, params = {}) {
      params['collection'] = options.collection;
      return options.apiBase + (type === 'click-history' ? Api.clickUrl : Api.searchUrl) + '?' + $.param(params);
    }
  };

  const View = {
    element: null,
    pageElements: [],
    isHidden: true,

    init: function(options) {
      View.element = ElementUtil.findOnce(options.box.selector);
      for (var i = 0, len = options.box.pageSelector.length; i < len; i++) {
        const el = ElementUtil.findOnce(options.box.pageSelector[i]);
        if (el) View.pageElements.push(el);
      }
      Constructor.prototype.hide();

      // create DOM element of back button from history box to results
      const backEl = ElementUtil.create('flb-history-box-back', View.element, 'a', Templates.getIconLabelTemplate(options, options.box.backIcon, options.box.backLabel), {style: 'cursor: pointer'});
      ElementUtil.addEvent(backEl, 'click', Constructor.prototype.hide);
      // create DOM element with main history header
      ElementUtil.create('flb-history-box-header', View.element, 'h2', Templates.getIconLabelTemplate(options, options.box.icon, options.box.label));
      View.element.insertAdjacentHTML('beforeend', Templates.once.box);
      // create DOM element to toggle history box display
      const trigger = ElementUtil.create('flb-history-trigger', ElementUtil.findOnce(options.trigger.selector), 'a', HandlebarsUtil.compile(options.trigger.template)(Templates.getIconLabelData(options, options.trigger.icon, options.trigger.label)), {style: 'cursor: pointer'});
      ElementUtil.addEvent(trigger, 'click', Constructor.prototype.toggle);

      View.render(options, 'click-history', 'clickBox'); // render display of click history box
      View.render(options, 'search-history', 'searchBox'); // render display of search history box
    },

    /**
     * On clearing hisotry data, remove items from history list and display no result message
     * - history options
     * - type of history box: `search-history` or `click-history`
     */
    clear: function(options, type) {
      ElementUtil.remove(ElementUtil.findOnce('.' + View.classType(type, 'results')));
      View.noResults(type, ElementUtil.findOnce('.' + View.classType(type, 'clear'), View.element), ElementUtil.findOnce('.' + View.classType(type), View.element));
    },

    /**
     * Render display of specific history box
     * - history options
     * - type of history box: `search-history` or `click-history`
     * - option name holding specific box options: `searchBox` or `clickBox`
     */
    render: function(options, type, box) {
      // find DOM elemenet holding specific history box
      const element = ElementUtil.findOnce('.' + View.classType(type), View.element),
        // create DOM elemenet with specific history box header
        headerEl = ElementUtil.create(View.classType(type, 'header'), element, 'h3', Templates.getIconLabelTemplate(options, options[box].icon, options[box].label)),
        // creat DOM element of button to clear all data for specific history box
        clearEl = ElementUtil.create(View.classType(type, 'clear'), headerEl, 'a', Templates.getIconLabelTemplate(options, options.box.clearIcon, options.box.clearLabel), {class: options.box.clearClasses});
      ElementUtil.addEvent(clearEl, 'click', function(e) {
        e.preventDefault();
        return Constructor.prototype.clear(options, type);
      });

      // find DOM element with list of specific history results
      const resultsEl = ElementUtil.findOnce(options[box].results);
      if (resultsEl) {
        const content = ElementUtil.getContent(resultsEl).trim();
        // if there is a content display results
        if (content.length) ElementUtil.create(View.classType(type, 'results'), element, 'div', content);
        // else display no results message
        else View.noResults(type, clearEl, element);
      }
      else Log.warn('No element was found with provided page selector "' + options[box].results + '"');
    },

    /**
     * Get CSS class selector based on history box type
     * - type of history box: `search-history` or `click-history`
     * - suffix to add to class
     */
    classType: function(type, s) {
      return 'flb-' + type + '-box' + (s ? '-' + s : '');
    },

    /**
     * Get label based on history box type
     * - type of history box: `search-history` or `click-history`
     */
    labelType: function(type) {
      return type.split('-').join(' ');
    },

    /**
     * Display no results message and hide clear all data button
     * - type of history box: `search-history` or `click-history`
     * - DOM element holding clear button
     * - DOM element holding specifing history box
     */
    noResults: function(type, clearEl, boxEl) {
      clearEl.style.display = 'none';
      ElementUtil.create(View.classType(type, 'empty'), boxEl, 'div', 'Your ' + View.labelType(type) + ' is empty', {class: 'text-muted'});
    },

    /**
     * Show or hide DOM elements in the page
     * - value of CSS property 'display'
     */
    togglePageElements: function(display) {
      for (var i = 0, len = View.pageElements.length; i < len; i++) {
        View.pageElements[i].style.display = display;
      }
    }
  };

  const Templates = {
    // List of partia templates registered with Handlebars
    // Usage ie. {{>icon-block}}, {{>label-block}}
    partial: {
      icon: '{{#if icon}}<span class="{{icon}} {{classes}} flb-cart-icon"></span>{{/if}}',
      label: '{{#if label}}{{label}}{{/if}}',
    },
    once: {
      iconLabel: '{{>icon-block}} {{>label-block}}',
      box: '<div class="row"><div class="col-md-6 flb-click-history-box"></div><div class="col-md-6 flb-search-history-box"></div></div>',
    },

    // Data model for Handlebars template
    getIconLabelData: function(options, icon, label) {
      return {icon: icon ? options.iconPrefix + icon : null, label: label};
    },

    // Get compiled Handlebars `iconLabel` template with data
    getIconLabelTemplate: function(options, icon, label) {
      const template = HandlebarsUtil.compile(Templates.once.iconLabel);
      return template(Templates.getIconLabelData(options, icon, label));
    }
  };

  /** Helpers */

  // Helpers to work with DOM elements
  const ElementUtil = {
    // Create DOM element
    create: function(id, context, tag, content, attrs) {
      const el = document.createElement(tag ? tag : 'div');
      if (content) ElementUtil.setContent(el, content);
      if (!attrs) attrs = {};
      if (attrs['class']) attrs['class'] += ' ' + id;
      else attrs['class'] = id;
      ElementUtil.setAttr(el, attrs);
      if (context) context.appendChild(el);
      return el;
    },

    // Remove DOM element
    remove: function(element) {
      element.parentNode.removeChild(element);
    },

    // Find all DOM elements with provided CSS selector
    find: function(selector, context) {
      if (!context) context = document;
      return context.querySelectorAll(selector);
    },

    // Find first DOM element with provided CSS selector
    findOnce: function(selector, context) {
      return ElementUtil.find(selector, context)[0];
    },

    // Set attribute of DOM element
    setAttr: function(element, attrs) {
      const attrsArr = Object.entries(attrs);
      for (var i = 0, len = attrsArr.length; i < len; i++) element.setAttribute(attrsArr[i][0], attrsArr[i][1]);
    },

    // Get content of DOM element
    getContent: function(element) {
      return element.innerHTML;
    },

    // Set content of DOM element
    setContent: function(element, content) {
      element.innerHTML = content;
    },

    // Assign event to DOM element
    addEvent: function(element, type, handler) {
      element.addEventListener(type, handler);
    },
  };

  // Helper to log into browser console
  const Log = {
    title: 'Funnelback History Widget',

    error: function(a1, a2, a3) {
      this.factory('error', a1, a2, a3);
    },

    warn: function(a1, a2, a3) {
      this.factory('warn', a1, a2, a3);
    },

    factory: function(type, a1, a2, a3) {
      console.group(Log.title);
      console[type](a1 || '', a2 || '', a3 || '');
      console.groupEnd();
    }
  };

  // Misc helpers
  const Utils = {
    // Deep extend of one object with properites of other object
    extend: function(obj, src) {
      for (var key in src) {
        if (src.hasOwnProperty(key)) obj[key] = typeof src[key] === 'object' ? Utils.extend(obj[key] || {}, src[key]) : src[key];
      }
      return obj;
    }
  };

  // Handlebars
  const HandlebarsUtil = {
    // Compile Handlebars template
    compile: function(template) {
      return Constructor.prototype.Handlebars.compile(template);
    },

    // Register with Handlebars partial templates
    registerPartial: function(templates) {
      const templatesArr = Object.entries(templates);
      for (var i = 0, len = templatesArr.length; i < len; i++) Constructor.prototype.Handlebars.registerPartial(templatesArr[i][0] + '-block', templatesArr[i][1]);
    },
  };

  HandlebarsUtil.registerPartial(Templates.partial);

  return Constructor;
}());