/*
 * Funnelback Cart Widget
 * version 0.1.0
 *
 * author: Liliana Nowak
 * Copyright Funnelback, 2019
 *
 * @requires Handlebars http://handlebarsjs.com@4.1.2
 * 
 * @usage 
 * initiation: var myCart = new Cart({ collection: 'my-collection-name' });
 * open cart: myCart.show();
 * hide cart: myCart.hide();
 * toggle visibility of cart: myCart.toggle();
 * get all settings of cart: myCart.getOption();
 * clear all cart data: myCart.clear(myCart.getOption());
 * add new item to cart: myCart.addItem(myCart.getOption(), 'http://someurl.com');
 * remove item from cart: myCart.deleteItem(myCart.getOption(), 'http://example.com');
 */
var Cart = (function() {
  'use strict'

  var Constructor = function(options) {
    return this.init(options);
  }

  // Default options
  Constructor.defaults = {
    apiBase: '/', // web service URL to get cart data from
    collection: null, // collection name; required parameter
    iconPrefix: 'glyphicon glyphicon-', // CSS class(es) prefix used to display icons
    cart: {
      selector: '#search-cart', // CSS selector to element where content of cart should be displayed
      pageSelector: ['#search-results-display', '#search-history'], // set of CSS selectors to parts of page to hide then when cart is displayed
      icon: 'pushpin', // icon to display for cart header; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      label: 'Saved', // label to display as cart header
      backIcon: 'arrow-left', // icon to display in cart for link to return to result page; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      backLabel: 'Back to results', // label to display in cart for link to return to result page
    },
    cartCount: {
      selector: '.flb-cart-count', // CSS selector to element where cart count should be displayed
      icon: 'shopping-cart', // icon to display for cart count; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      isLabel: false, // [true|false]; if true label will be displayed as text of element else it won't be displayed
      label: 'cart', // label add to attribute `title` of cart count to inform about number of items; if `isLabel: true`, this label will be displayed as element text
      template: '{{>icon-block}} {{>label-block}}{{>badge-block}}'
    },
    item: {
      selector: '#search-results', // CSS selector to list of item; if item should be toggled into cart, item requires to have attribute 'data-cart-url' that has index URL value of item
      template: '<h4><a href="{{indexUrl}}">{{#truncate 70}}{{title}}{{/truncate}}</a></h4><cite class="text-success">{{#cut "https://"}}{{indexUrl}}{{/cut}}</cite><p>{{#truncate 255}}{{summary}}{{/truncate}}</p>',
    },
    itemTrigger: {
      // Where item trigger should be displayed within context of `itemSelector`
      selector: 'h4', // CSS selector of element where item trigger will be inserted in relative position to it
      position: 'afterbegin', // [beforebegin|afterbegin|beforeend|afterend] relative position to `selector` element where item trigger will be inserted
      /*
        beforebegin: before `selector` element itself
        afterbegin: just inside the `selector` element, before its first child
        beforeend: just inside the `selector` element, after its last child
        afterend: after the `selector` element itself
       */
      // Set display of item trigger to add to / delete from cart
      iconAdd: 'pushpin', // icon to display for trigger to add item to cart; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      iconDelete: 'remove', // icon to display for trigger to delete item from cart; will be prefixed with `iconPrefix`; if null/undefined, no icon will be displayed
      isLabel: false, // [true|false]; if false `labelAdd`\`labelDelete` will be displayed as `title` attribute of element else will be displayed as text of element
      labelAdd: 'Add to cart', // label to display for trigger to add item to cart
      labelDelete: 'Remove from cart', // label to display for trigger to delete item from cart
      template: '{{>icon-block}} {{>label-block}}',
    }
  }

  /**
   * Create isolated Handlebars environment
   */
  Constructor.prototype.Handlebars = Handlebars.create();

  /**
   * Initialise cart widget with provided options
   * - initiliase cart views and triggers
   * - fetch cart data
   */
  Constructor.prototype.init = function(options) {
    if (!options.collection) {
      Log.error('Missing "collection" parameter');
      return null;
    }

    this.options = Utils.extend(Constructor.defaults, options || {});
    if (!this.options.cart.pageSelector) this.options.cart.pageSelector = [];

    CartBox.init(this.options);
    CartCount.init(this.options);
    Item.init(this.options);
    ItemTrigger.init(this.options);
    Items.set(this.options);

    const that = this;
    Api.get(that.options).then(function(response) {
      Items.update(that.options, response.data);
      CartCount.set(response.data.length);
    }).catch(function(error) {
      Log.error('Something went wrong and no data was fetched. Try again later..', error);
    });
    return this;
  };

  /**
   * Clear all items from cart
   */
  Constructor.prototype.clear = function(options) {
    if (confirm('Your selection will be cleared') === true) {
      Api.delete(options).then(function(response) {
        Items.clear(options);
        CartCount.set(response.data.length);
        Constructor.prototype.hide();
      }).catch(function(error) {
        Log.error('Something went wrong and cart was not cleared. Try again later..', error);
      });
    }
    return this;
  };

  /**
   * Destroy cart widget, erase options
   */
  Constructor.prototype.destroy = function() {
    this.options = {};
    return null;
  };

  /**
   * Open cart
   */
  Constructor.prototype.show = function() {
    CartBox.element.style.display = 'block';
    CartBox.togglePageElements('none');
    CartBox.isHidden = false;
    return this;
  };

  /**
   * Close cart
   */
  Constructor.prototype.hide = function() {
    CartBox.element.style.display = 'none';
    CartBox.togglePageElements('block');
    CartBox.isHidden = true;
    return this;
  };

  /**
   * Toggle cart
   */
  Constructor.prototype.toggle = function() {
    return CartBox.isHidden ? Constructor.prototype.show() : Constructor.prototype.hide();
  };

  /**
   * Get/set cart settings
   * - no arguments - get all settings
   * - one argument `key` - get value of specific setting for that argument
   */
  Constructor.prototype.getOption = function(key) {
    if (arguments.length === 0) {
      return this.options; // get all options
    }

    if (typeof key === 'string') {
      return this.options[key]; // get value of specific option
    }
  };

  /**
   * Add new item to cart
   * - cart options
   * - url of item to be added to cart
   */
  Constructor.prototype.addItem = function(options, url) {
    Api.post(options, {url: url}).then(function(response) {
      Item.update(options, response.data.filter(it => it.indexUrl === url)[0], 'del');
      CartCount.set(response.data.length);
    }).catch(function(error) {
      Log.error('Something went wrong and item was not saved in a cart. Try again later..', error);
    });
  };

  /**
   * Remove item from cart
   * - cart options
   * - url of item to be removed from cart
   */
  Constructor.prototype.deleteItem = function(options, url) {
    Api.delete(options, {url: url}).then(function(response) {
      Item.update(options, {indexUrl: url}, 'add');
      CartCount.set(response.data.length);
    }).catch(function(error) {
      Log.error('Something went wrong and result was not removed from a cart. Try again later..', error);
    });
  };

  // Handle API request
  const Api = {
    urlPath: 's/cart.json',

    delete: function(options, params) {
      return Api.request('delete', options, params);
    },

    get: function(options, params) {
      return Api.request('get', options, params);
    },

    post: function(options, params) {
      return Api.request('post', options, params);
    },

    request: function(method, options, params) {
      return new Promise(function(resolve, reject) {
        const xhr = new XMLHttpRequest(), url = Api.getUrl(options, params);
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

    getUrl: function(options, params = {}) {
      params['collection'] = options.collection;
      return options.apiBase + Api.urlPath + '?' + $.param(params);
    }
  };

  // Hadnler to access and create cart
  const CartBox = {
    element: null, // DOM element displying cart
    listElement: null, // DOM element displaying list of cart items inside cart
    pageElements: [], // DOM element to whole page to hide it when cart is displayed
    isHidden: true, // state of visibility of cart

    init: function(options) {
      CartBox.element = ElementUtil.findOnce(options.cart.selector);
      if (!CartBox.element) Log.warn('No element was found with provided selector "' + options.cart.selector + '"');
      
      for (var i = 0, len = options.cart.pageSelector.length; i < len; i++) {
        const el = ElementUtil.findOnce(options.cart.pageSelector[i]);
        if (el) CartBox.pageElements.push(el);
      }
      if (!CartBox.pageElements.length) Log.warn('No element was found with provided page selector "' + options.cart.pageSelector + '"');
      
      Constructor.prototype.hide();

      const template = HandlebarsUtil.compile(Templates.once.iconLabel),
        // create DOM element of back button from cart to results
        backEl = ElementUtil.create('flb-cart-box-back', CartBox.element, 'a', template({icon: options.cart.backIcon ? options.iconPrefix + options.cart.backIcon : null, label: options.cart.backLabel}), {style: 'cursor: pointer'}),
        // create DOM elemenet of cart header
        headerEl = ElementUtil.create('flb-cart-box-header', CartBox.element, 'h2', template({icon: options.cart.icon ? options.iconPrefix + options.cart.icon : null, label: options.cart.label})),
        // creat DOM element of button to clear all data in cart
        clearEl = ElementUtil.create('flb-cart-box-clear', headerEl, 'a', template({icon: options.iconPrefix + 'remove', label: 'Clear'}), {class: 'btn btn-xs btn-danger'});
      ElementUtil.addEvent(backEl, 'click', Constructor.prototype.hide);
      ElementUtil.addEvent(clearEl, 'click', function() { return Constructor.prototype.clear(options); });
      // create DOM element of list of cart items
      CartBox.listElement = ElementUtil.create('flb-cart-box-list', CartBox.element, 'ul', null, {class: 'list-unstyled'});
    },

    /**
     * Show or hide DOM elements in the page
     * - value of CSS property 'display'
     */
    togglePageElements: function(display) {
      for (var i = 0, len = CartBox.pageElements.length; i < len; i++) {
        CartBox.pageElements[i].style.display = display;
      }
    }
  };

  // Handler to access and create element displaying cart count on page
  const CartCount = {
    selector: 'flb-cart-count-trigger', // CSS class assigned to cart count element
    element: null, // DOM element displayin cart count
    icon: null, // prefixed with `iconPrefix` icon to display for cart count; if null no icon is displayed
    label: null, // label to display fir cart count if label is enabeld `isLabel: true`
    partialTitle: ' items', // partial title used to create content of title attribute "<count> items"; if label is set "<count> item in your <label>"
    template: null, // compiled Handlebars template to display cart count

    init: function(options) {
      if (options.cartCount.icon) CartCount.icon = options.iconPrefix + options.cartCount.icon;
      if (options.cartCount.isLabel) CartCount.label = options.cartCount.label;
      if (options.cartCount.label) CartCount.partialTitle += ' in your ' + options.cartCount.label.toLowerCase();
      CartCount.template = HandlebarsUtil.compile(options.cartCount.template);
      CartCount.element = ElementUtil.create(CartCount.selector, ElementUtil.findOnce(options.cartCount.selector), 'a', CartCount.template(CartCount.data(0)), {href: '#', title: CartCount.title(0)});
      ElementUtil.addEvent(CartCount.element, 'click', Constructor.prototype.toggle);
    },

    // Data model for Handlebars template
    data: function(count) {
      return {count: count, icon: CartCount.icon, label: CartCount.label};
    },

    // Update count and title of cart count
    set: function(count) {
      ElementUtil.setContent(CartCount.element, CartCount.template(CartCount.data(count)));
      CartCount.element.setAttribute('title', CartCount.title(count));
    },

    // Get title attribute content of cart count
    title: function(count) {
      return count + CartCount.partialTitle;
    },
  };

  // Handler to access and update search result/cart item
  const Item = {
    selectorAttr: 'data-cart-url', // name of attribute holding index URL of search result that should be toogled into cart
    listElement: null, // DOM element with list of search results
    template: null, // compiled Handlebars template to display single item in cart

    init: function(options) {
      if (options.item.template) Item.template = HandlebarsUtil.compile(options.item.template);
      Item.listElement = ElementUtil.findOnce(options.item.selector);
      if (!Item.listElement) Log.warn('No element was found with provided selector "' + options.item.selector + '"');
    },

    // Get CSS selector to search result with provided index URL
    // if no `url` is passed get CSS selector to all search results
    selector: function(url) {
      return url ? '[' + Item.selectorAttr + '="' + url + '"]' : '[' + Item.selectorAttr + ']';
    },

    // Toggle display of cart trigger for search result
    // Toggle display of item in cart
    update: function(options, data, type) {
      // Find cart trigger within serach result
      const itemTrigger = ElementUtil.findOnce(Item.selector(data.indexUrl), Item.listElement);
      // Toggle display of cart trigger
      if (itemTrigger) ItemTrigger.update(options, itemTrigger, type);

      if (type === 'add') {
        // Find cart item to be removed from cart display
        const cartItem = ElementUtil.findOnce(Item.selector(data.indexUrl), CartBox.listElement);
        // Remove cart item from cart
        ElementUtil.remove(cartItem);
      } else {
        // Create new item to be displayed in cart
        const cartItem = ElementUtil.create('flb-cart-box-item', null, 'li', Item.template(data), {'data-cart-url': data.indexUrl});
        // Create cart trigger for new item in cart
        ItemTrigger.set(options, cartItem);
        // Set trigger to be delete trigger for new item in cart
        ItemTrigger.update(options, cartItem);
        // Add new item at the beginning of list of items in cart
        CartBox.listElement.insertAdjacentElement('afterbegin', cartItem);
      }
    }
  };

  const Items = {
    // On clearing cart data, update display of item in cart and cart triggers within search results
    clear: function(options) {
      // Find all search results
      const items = ElementUtil.find(Item.selector(), Item.listElement);
      // Remove items from cart and set cart tirggers within search results to be added
      for (var i = 0, len = items.length; i < len; i++) ItemTrigger.update(options, items[i], 'add');
    },

    // Initialise cart triggers to search results
    set: function(options) {
      // Find all search results
      const items = ElementUtil.find(Item.selector(), Item.listElement);
      // Set cart triggers withing search results to be added
      for (var i = items.length - 1; i >= 0; i--) ItemTrigger.set(options, items[i]);
    },

    // Update display of items in cart and cart triggers within search results based on fetched cart data
    update: function(options, data, type) {
      for (var i = 0, len = data.length; i < len; i++) Item.update(options, data[i], type);
    }
  }

  // Handler to access and create cart trigger
  const ItemTrigger = {
    selector: 'flb-cart-item-tirgger', // CSS class name assigned to each cart trigger
    addEvent: null, // click event assigned to cart trigger to add item to cart
    addTemplate: null, // compiled Handlebars template of cart trigger to display add to cart
    addTitle: 'Add', // title used to create content of title attribute of cart trigger to display for add to cart
    delEvent: null, // click event assigned to cart trigger to remove item from cart
    delTemplate: null, // compiled Handlebars template of cart trigger to display remove from cart
    delTitle: 'Remove', // title used to create content of title attribute of cart trigger to display for remvoe from cart

    init: function(options) {
      const template = HandlebarsUtil.compile(options.itemTrigger.template);
      ItemTrigger.addTemplate = template({icon: options.iconPrefix + options.itemTrigger.iconAdd, label: options.itemTrigger.isLabel ? options.itemTrigger.labelAdd : null});
      ItemTrigger.delTemplate = template({icon: options.iconPrefix + options.itemTrigger.iconDelete, label: options.itemTrigger.isLabel ? options.itemTrigger.labelDelete : null});
      if (options.cartCount.label) {
        ItemTrigger.addTitle += ' to ' + options.cartCount.label.toLowerCase();
        ItemTrigger.delTitle += ' from ' + options.cartCount.label.toLowerCase();
      }
      ItemTrigger.addEvent = function(e) {
        const item = e.currentTarget.closest(Item.selector()), url = item.getAttribute(Item.selectorAttr);
        if (url) return Constructor.prototype.addItem(options, url);
        else Log.warn('No URL found to save item in a cart');
      };
      ItemTrigger.delEvent = function(e) {
        const item = e.currentTarget.closest(Item.selector()), url = item.getAttribute(Item.selectorAttr);
        if (url) return Constructor.prototype.deleteItem(options, url);
        else Log.warn('No URL found to remove result from a cart');
      };
    },

    // Create cart trigger for provided cart item or search result
    set: function(options, item) {
      const el = ElementUtil.findOnce(options.itemTrigger.selector, item);
      const trigger = ElementUtil.create(ItemTrigger.selector, null, 'a', ItemTrigger.addTemplate, {style: 'cursor: pointer', title: ItemTrigger.addTitle});
      ElementUtil.addEvent(trigger, 'click', ItemTrigger.addEvent);
      if (el) el.insertAdjacentElement(options.itemTrigger.position, trigger);
      else Log.warn('No element was found with provided selector "' + options.itemTrigger.selector + '"');
    },

    // Toggle display of cart trigger
    update: function(options, item, type) {
      const el = ElementUtil.findOnce('.' + ItemTrigger.selector, item);
      if (type === 'add') {
        ElementUtil.setContent(el, ItemTrigger.addTemplate);
        ElementUtil.removeEvent(el, 'click', ItemTrigger.delEvent);
        ElementUtil.addEvent(el, 'click', ItemTrigger.addEvent);
        el.setAttribute('title', ItemTrigger.addTitle);
      } else {
        ElementUtil.setContent(el, ItemTrigger.delTemplate);
        ElementUtil.removeEvent(el, 'click', ItemTrigger.addEvent);
        ElementUtil.addEvent(el, 'click', ItemTrigger.delEvent);
        el.setAttribute('title', ItemTrigger.delTitle);
      }
    }

  };

  const Templates = {
    // List of partia templates registered with Handlebars
    // Usage ie. {{>icon-block}}, {{>badge-block}}
    partial: {
      badge: '<span class="badge">{{count}}</span>',
      icon: '{{#if icon}}<span class="{{icon}} {{classes}} flb-cart-icon"></span>{{/if}}',
      label: '{{#if label}}{{label}}{{/if}}',
    },
    once: {
      iconLabel: '{{>icon-block}} {{>label-block}}',
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

    // Set content of DOM element
    setContent: function(element, content) {
      element.innerHTML = content;
    },

    // Assign event to DOM element
    addEvent: function(element, type, handler) {
      element.addEventListener(type, handler);
    },
    
    // Remove event from DOM element
    removeEvent(element, type, handler) {
      element.removeEventListener(type, handler);
    },
  };

  // Helper to log into browser console
  const Log = {
    title: 'Funnelback Cart Widget',

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

  // Misc
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

  Constructor.prototype.Handlebars.registerHelper({
    // Cut the left part of a string if it matches the provided `toCut` string
    // Usage: {{#cut "https://"}}{{indexUrl}}{{/cut}}
    cut: function(toCut, options) {
      const str = options.fn(this);
      if (str.indexOf(toCut) === 0) return str.substring(toCut.length);
      return str;
    },
    // Truncate content to provided lenght
    // Usage: {{#truncate 70}}{{title}}{{/truncate}}
    truncate: function (len, options) {
      const str = options.fn(this);
      if (str && str.length > len && str.length > 0) {
          var new_str = str + " ";
          new_str = str.substr (0, len);
          new_str = str.substr (0, new_str.lastIndexOf(" "));
          new_str = (new_str.length > 0) ? new_str : str.substr (0, len);
          return new Constructor.prototype.Handlebars.SafeString (new_str +'...'); 
      }
      return str;
    }
  });

  return Constructor;
}());
