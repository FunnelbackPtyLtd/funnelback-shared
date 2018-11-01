/*
 * Funnelback Knowledge Graph plugin
 * version 2.7
 *
 * author: Liliana Nowak
 * Copyright Funnelback, 2017-2018
 *
 * @requires jQuery https://jquery.com@1.10.2
 * @requires Handlebars http://handlebarsjs.com@4.0.10
 * @requires moment.js http://momentjs.com@2.18.2
 * @requires nprogress.js http://ricostacruz.com/nprogress@0.2.0
 */
(function($) {
  'use strict';

  var _box = {};

  var KnowledgeGraph = function(element, options) {
    if (!options.collection || !options.profile) {
      Log.warn('Missing "collection" and/or "profile" parameter');
      return null;
    }
    this.set(element, options).init();
    return this;
  };

  KnowledgeGraph.defaults = {
    apiBase: '/',
    contentFetcher: 'nodes',
    contentSelector: null,
    contentType: 'text', // [text|attr]
    contentAttr: null,
    targetUrl: true,
    dateFormat: 'DD MMMM YYYY, HH:mm', // moment.js date formatting
    maxBreadcrumb: 5,
    maxPagination: 5,
    maxResults: 10,
    searchUrl: null,
    searchParams: {
      SF: '[^(?i)(?!Fun).*,FUNfkgNodeLabel]',
    },
    iconPrefix: 'fa fa-fw fa-',
    trigger: 'button', // [button|fixed|full]
  };

  KnowledgeGraph.Handlebars = Handlebars.noConflict();

  KnowledgeGraph.prototype.get = function() {
    return _box;
  };

  KnowledgeGraph.prototype.set = function(element, options) {
    var box = $.extend(true, {}, Box.instance);
    box.id  = Math.floor(Math.random() * 100) + 1;
    box.context = (element instanceof HTMLDocument || element === window) ? 'body' : element;
    box.options = this.option(box, options);
    View.configure(box);
    _box = box;
    return this;
  };

  KnowledgeGraph.prototype.init = function() {
    const instance = this.get();
    ProgressBar.start();

    Promise.all([Template.configure(instance), Translation.configure(instance)]).then(function() {
    }).catch(function(error) {
      Api.error(error, true);
    }).then(function() {
      if (instance.data) instance.data();
    });

    return this;
  };

  KnowledgeGraph.prototype.option = function(box, key, val) {
    if (arguments.length === 1) return box.options;

		var options = $.isObject(key) ? key : {};
		if ($.isString(key)) {
			if (arguments.length === 2 || !$.isDefined(val)) return $.dataVals($.extend({}, box.options), key);
			options[key] = val;
		}

    for (var k in options) _setOption(k, options[k]);
    return options;

		function _setOption(key, val) {
      if (key === 'apiBase' && val) options[key] = Url.setBase(val);
      if (key === 'contentFetcher') box.data = Api[val];
      if (key === 'contentSelector' && val) options[k] = $.isString(val) ? [val] : val || [];
      if (key === 'maxResults' && val) box.results.size = parseInt(val);
      if (key === 'templatesFile' && val) options[k] = !val.match(/^[a-zA-Z]+:\/\//) ? Url.get(val, null, options['apiBase']) : val;
      if (key === 'trigger') {
        if (Box.triggers.indexOf(val) < 0) val = Box.triggers[0];
        options[k] = val;
        box.view  = val === 'full' || val === 'fixed' ? val : 'auto';
        const t = box.view.capitalize();
        box.open  = Box['open' + t];
        box.close = Box['close' + t];
      }
		}
  };

  const _prefix='flb-';
  const _title = 'Funnelback Knowledge Graph';

  /**
   * Data handle
   */

  // Handle API request and responses
  const Api = {
    urlPrefix: 'fkg/nodes',

    keywordsV: 1,
    keywords: function() {
      const box = this;
      Api.get(box, Url.get('nodes', {keywords: Api.getInitContent(box)}, box.options.apiBase), Data.processNodes).then(function(response) {
        if (box.options.trigger === 'button') {
          View.nodesButton(response.box, response.url, response.data);
        } else {
          View.init(response.box);
          View.nodesList(response.box, response.url, response.data);
        }
      }).catch(function(error) {
        Api.error(error);
      }).then(function() {
        ProgressBar.stop();
      });
    },

    nodes: function() {
      const box = this;
      Api.get(box, Api.getUrl(box, Api.resolveId(box)), Data.processNodes).then(function(response) {
          if (box.options.trigger === 'button') {
          View.nodesButton(response.box, response.url, response.data);
        } else {
          View.init(response.box);
          box.options.trigger === 'full'
            ? View.nodesDetails(response.box, response.url, response.data)
            : View.nodesList(response.box, response.url, response.data);
        }
      }).catch(function(error) {
        Api.error(error);
        if (box.options.trigger === 'full' && error.box && error.box.container) error.box.open();
      }).then(function() {
        ProgressBar.stop();
      });
    },

    getUrl: function(box, id) {
      return Url.get(Api.urlPrefix, {liveUrl: id, collection: box.options.collection, profile: box.options.profile}, box.options.apiBase);
    },

    resolveId: function(box) {
      const targetUrlParam = Url.getPathParams(window.location.search).targetUrl;
      var id = window.location.href;
      if (targetUrlParam) id = targetUrlParam;
      else if (box.options.targetUrl && $.isString(box.options.targetUrl)) id = box.options.targetUrl;
      else if (box.options.contentSelector) {
        const sel = document.querySelector(box.options.contentSelector);
        id = undefined;
        if (sel) id = (box.options.contentType === 'attr' ? sel.getAttribute(box.options.contentAttr) : sel.innerText).trim();
        else Log.warn('Element "' + box.options.contentSelector + '" has not been found');
      }
      return id;
    },

    // Fetch data
    error: function(data, inConsole) {
      if (data.box) {
        try {
          const res = data.error.response ? JSON.parse(data.error.response) : '';
          if (inConsole) {
            Log.log(data.url, res.status, res.error);
          } else {
            if (res.status && res.status != 500) View.error(data.box, res.status, res.error);
            else View.error(data.box, res.status, 'Something went wrong');
          }
        } catch(error) {
          Log.log(error);
        }
      }
    },

    get: function(box, url, process, params) {
      return Api.request(box, 'get', url, params, process);
    },

    post: function(box, url, process, params) {
      return Api.request(box, 'post', url, params, process);
    },

    request: function(box, method, url, params, process) {
      return new Promise(function(resolve, reject) {
        const storageKey = Url.storageKey(url, box.options.apiBase);
        if (Storage.isItem(storageKey)) {
          resolve({box: box, url: url, data: Storage.getItem(storageKey)});
          return null;
        }

        var xhr = new XMLHttpRequest();
        // Setup callbacks
        xhr.onload = function() {
          if (this.status !== 200) { // If the request failed
            reject({box: box, url: url, error: this});
          } else { // If the request succeeded
            try {
              const data = JSON.parse(this.responseText);
              resolve({box: box, url: url, data: process && $.isFunction(process) ? process(box, url, data) : data});
            } catch(error) {
              reject({box: box, url: url, error: error});
            }
          }
        }

        xhr.onerror = function() {
          reject({box: box, url: url, error: xhr});
        }

        xhr.open(method, url, true);
        xhr.setRequestHeader('Accept', 'application/json');
        xhr.setRequestHeader('Content-Type', 'text/plain');
        xhr.send(params || {});
      });
    }
  };

  // Data processing
  const Data = {
    // View usage
    convertToView: function(box, data, fieldType) {
      var i, len = data.length, template;
      for (i = 0; i < len; i++) {
        template = Template.get(box, 'fields', fieldType, data[i]._type);
        getFields(template, 'primary', data[i]);
        getFields(template, 'secondary', data[i]);
      }
      return data;

      function getFields(list, type, data) {
        data[type] = [];
        if (!list[type]) return;
        for (var i = 0, len = list[type].length; i < len; i++) {
          if (data['_fields'][list[type][i]]) data[type].push({key: list[type][i], val: data['_fields'][list[type][i]].toLocaleString()});
        }
      }
    },

    groupNodes: function(box, data, type, rels) {
      var i, len, grouped = [], groupName, currentGroup;
      if (box.template.group[type]) {
        if ($.isString(box.template.group[type])) groupName = box.template.group[type];
        else groupName = box.template.group[type][rels] || box.template.group[type][Template.keyDefault];
      }

      for (i = 0, len = data.length; i < len; i++) {
        if (groupName && data[i]['_fields'][groupName] && data[i]['_fields'][groupName].length) {
          var groupLabel = Template.isDateField(groupName) ? Dates.range(data[i]['_fields'][groupName][0]) : data[i]['_fields'][groupName][0];
          if (groupLabel != currentGroup) {
            currentGroup = groupLabel;
            grouped.push({group: groupLabel});
          }
        }
        grouped.push(data[i]);
      }

      return grouped;
    },

    // Map data
    mapLinks: function(data) {
      var i, len, res = {};
      for (i = 0, len = data.links.length; i < len; i++) res[data.links[i].rel] = data.links[i].href;
      return res;
    },

    // Process data
    processNodes: function(box, url, data) {
      const storageKey = Url.storageKey(url, box.options.apiBase);        // Autolink can return one item multiple times; instead of reprocessing it again
      if (Storage.isItem(storageKey)) return Storage.getItem(storageKey); // read processed value from storage

      const processedData = Model.retrieve(box, data.nodes, 'graph', 'node');
      var i, len, nodesIds = [];
      for (i = 0, len = processedData.length; i < len; i++) {
        Storage.setItem(Url.storageKey(processedData[i]._url.self, box.options.apiBase), {list: [processedData[i]], total: 1});
        nodesIds.push(processedData[i]._id);
      }
      if (data.nodesSummary.totalMatching > 1 || !Url.isNodeDetail(url)) Storage.setItem(Url.storageKey(url, box.options.apiBase), {list: nodesIds, total: data.nodesSummary.totalMatching}); // Store only actual list with 2 or more elements
      return {list: processedData, total: data.nodesSummary.totalMatching};
    },

    processRels: function(box, url, data) {
      const processedData = Model.retrieve(box, data, 'graph', 'rel');
      Storage.setItem(Url.storageKey(url, box.options.apiBase), processedData);
      return processedData;
    },

    processTypes: function(box, url, data) {
      const processedData = Model.retrieve(box, data, 'graph', 'type');
      Storage.setItem(Url.storageKey(url, box.options.apiBase), processedData);
      return processedData;
    },

    processSearch: function(box, url, data) {
      var processedData = [], summary = {}, facets = [];
      if (data.response && data.response.resultPacket) {
        facets = data.response.facets || [];
        summary = Model.retrieve(box, [data.response.resultPacket], 'search', 'summary')[0];
        processedData = Model.retrieve(box, data.response.resultPacket.results, 'search', 'node');

        var i, len, j, lenj;
        for (i = 0, len = facets.length; i < len; i++) {
          if (facets[i].unselectAllUrl && facets[i].selected) {
            facets[i]['_url'] = Search.getUrl(box, facets[i].unselectAllUrl);
            facets[i]['_label'] = 'Clear all';
          }
          for (j = 0, lenj = facets[i].allValues.length; j < lenj; j++) {
            facets[i].allValues[j]['_url'] = Search.getUrl(box, facets[i].allValues[j].toggleUrl);
          }
        }

        for (i = 0, len = processedData.length; i < len; i++) {
          processedData[i]._url.self = Api.getUrl(box, processedData[i]._url.self);
        }
      }

      return {list: processedData, summary: summary, facets: facets};
    }
  };

  // Data storage
  const Storage = {
    data: {},

    clear: function() {
      Storage.data = {};
    },

    getItem: function(key) {
      var data = Storage.data[key];
      if (data && data.list && !$.isObject(data.list[0])) { // To not duplicate entries in storage, for list view only IDs are saved so data need to be retrieved first
        for (var i = 0, len = data.list.length; i < len; i++) data.list[i] = Storage.getItem('fkgnodes' + data.list[i]).list[0];
      }
      return data;
    },

    isItem: function (key) {
      return Storage.data.hasOwnProperty(key) && Storage.data[key] !== null;
    },

    removeItem: function(key) {
      delete Storage.data[key];
    },

    setItem: function(key, val) {
      Storage.data[key] = val;
    }
  };

  /**
   * Handle model
   */
  const Model = {
    _badges: ['resolution', 'status'],
    _fields: {title: 'title', subtitle: 'subtitle', desc: 'desc', img: 'img', viewUrl: 'url', list: {primary: [], secondary: []}, detail: {primary: [], secondary: []}},

    graph: {
      node: {_id: 'node.id', _url: {self: 'links.0.href', types: 'links.1.href', typesLeaf: 'links.2.href'}, _type: 'node.labels.0', _labels: 'node.labels', _fields: 'node.propertyList'},
      rel: {_id: ['relationship', 'direction.name'], _label: ['relationship', 'direction.name'], _count: 'rel_count', _url: 'links.0.href'},
      type: {_count: 'hierarchy.data.typeCount', _url: 'hierarchy.data.links.1.href', _type: 'type', _label: 'hierarchy.data.label'},
      pagination: {start: 'start', size: 'pageSize'},
    },

    search: {
      node: {_id: 'liveUrl', _url: {self: 'liveUrl'}, _type: 'listMetadata.FUNfkgNodeLabel.0', _labels: 'listMetadata.FUNfkgNodeLabel', _fields: 'listMetadata', _title: 'title', _desc: 'summary'},
      pagination: {start: 'start_rank', size: 'num_ranks'},
      summary: {_start: 'resultsSummary.currStart', _end: 'resultsSummary.currEnd', _query: 'query', _total: 'resultsSummary.totalMatching', _rank: 'resultsSummary.numRanks'}
    },

    retrieve: function(box, data, view, type, extend) {
      var processedData = [];
      if (Model[view] && Model[view][type]) {
        if (!extend) extend = {};
        processedData = getData(data, $.extend(true, Model[view][type], extend));
      }
      return processedData;

      function getData(data, model) {
        const keys = Object.keys(model), leni = data.length;
        var i, item, key, processedData = [];
        for (i = 0; i < leni; i++) {
          item = getItem(data[i], keys);
          if ($.isFunction(Model[type])) Model[type](box, item, view);
          if (item._fields) Model.fields(box, item);
          if (item._type) Model.icon(box, item);
          processedData.push(item);
        }
        return processedData;

        function getItem(data, keys) {
          var i, len = keys.length, item = {}, vals = [];
          for (i = 0; i < len; i++) {
            key = model[keys[i]];
            if (key) {
              item[keys[i]] = $.isString(key) ? $.dataVals(data, key) : ($.isArray(key) ? getItem(data, key) : getData([data], key)[0]);
            } else {
              vals.push($.dataVals(data, keys[i]));
            }
          }
          return vals.length ? vals.join('-') : item;
        }
      }
    },

    fields: function(box, data) {
      const fields = Object.keys(this._fields);
      var i, len, field;
      for (i = 0, len = fields.length; i < len; i++) {
        field = Template.get(box, 'fields', '_' + fields[i], data._type);
        if (field && $.isString(field) && data._fields[field]) data['_' + fields[i]] = data._fields[field];
      }
    },

    icon: function(box, data) {
      data['_icon'] = Template.get(box, 'icon', data._type);
    },

    rel: function(box, data) {
      const sortField = box.template.sort[Url.getPathParts(data._url, box.options.apiBase, 2)], url = Url.getUrl(data._url);
      var params = Url.getPathParams(data._url);
      if (sortField) {
        params['sort'] = sortField;
        data['_url'] = Url.get(url, params);
      }
      if (data['_label']) data['_label'] = data['_label'].replace('-', '.');
    },

    node: function(box, data, view) {
      if (view === 'graph') {}
      if (view === 'search') {
        if (data._type) data._type = data._type.toLowerCase();
      }
    }
  }

  /**
   * Handle and render view
   */
  const Box = {
    triggers: ['icon', 'button', 'link', 'fixed', 'full'],
    instance: {
      id: null,
      container: null,
      stackUrl: [],
      stackBreadcrumb: [],
      events: null,
      isOpen: false,
      open: null,
      close: null,
      results: {
        size: 10,
        total: null,
        url: null,
      },
      template: {
        fields: {},
        group: {},
        icon: {},
        sort: {},
      },
    },

    open: function(instance) {
      Modal.show(instance);
    },

    openAutoFromFixed: function(instance, $trigger) {
      instance.container.attr('data-fkg-context', 'auto');
      Utils.getElement(_prefix + 'modal', instance.container).removeClass(_prefix + 'modal-fixed').addClass(_prefix + 'modal-auto');
      Utils.getElement(_prefix + 'modal-dialog', instance.container).addClass(Template.modalWrapper.full);
      var partial = true;
      if($trigger.attr('data-fkg-nav') === 'go') {
        $trigger.attr('data-fkg-nav', 'init-go');
        partial = false;
      }
      View.init(instance, $trigger, partial);
      instance.options.modalBackdrop = true;
      instance.options.modalFocus = true;
      Modal.init(instance);
      Modal.showBackdrop(instance);
    },

    openAuto: function(trigger) {
      this.options.modalBackdrop = true;
      this.options.modalFocus = true;
      Box.open(this);
    },

    openFixed: function() {
      this.options.modalBackdrop = false;
      this.options.modalFocus = false;
      Box.open(this);
    },

    openFull: function() {
      this.options.modalBackdrop = false;
      this.options.modalFocus = true;
      Box.open(this);
    },

    closeAuto: function() {
      Modal.hide(this);
    },

    closeFixed: function() {
      if(!this.isOpen) return;
      const initial = Navigation.head(this);
      if (initial) {
        Api.get(this, initial._url).then(function(response) {
          View.init(response.box);
          View.nodesList(response.box, response.url, response.data);
        }).catch(function(error) {
          Api.error(error);
        });
      } else if (this.listWrapper[0].offsetHeight) Box.fullToFixed(this, 'detail');

      this.container.attr('data-fkg-context', 'fixed');
      Utils.getElement(_prefix + 'modal', this.container).removeClass(_prefix + 'modal-auto').addClass(_prefix + 'modal-fixed');
      Utils.getElement(_prefix + 'modal-dialog', this.container).removeClass(Template.modalWrapper.full);
      Modal.hideBackdrop(this);
      Modal.destroy(this);
      this.options.modalBackdrop = false;
    },

    closeFull: function() {},

    clean: function() {
      if (!arguments.length) return undefined;
      var i, len, items = [].slice.call(arguments);
      for (i = 0, len = items.length; i < len; i++) items[i].html('');
    },

    fixedToFull: function(box) {
      box.tabsWrapper.show();
      box.detailWrapper.removeClass(Template.detailWrapper.fix).addClass(Template.detailWrapper.full).show();
      box.listWrapper.removeClass(Template.listWrapper.fix).addClass(Template.listWrapper.full).show();
    },

    fullToFixed: function(box, context) {
      Breadcrumb.hide(box);
      if (context === 'detail') {
        box.detailWrapper.removeClass(Template.detailWrapper.full).addClass(Template.detailWrapper.fix);
        box.listWrapper.hide();
      } else {
        box.listWrapper.removeClass(Template.listWrapper.full).addClass(Template.listWrapper.fix);
        box.detailWrapper.hide();
        box.tabsWrapper.hide();
      }
    },
  };

  const Events = {
    eventSuffix: '.fkg',

    name: function(eventName) {
      return eventName + this.eventSuffix;
    },

    nav: function(box, callback) {
      Events.factory('nav', box, callback);
    },

    render: function(box, callback) {
      Events.factory('render', box, callback);
    },

    factory: function(eventName, box, callback) {
      if (!$.isFunction(callback)) return;

      box.container.bind(Events.name(eventName), function() {
        return callback.apply(null, [].slice.call(arguments));
      });
    }
  };

  const View = {
    init: function(box, obj, partial) {
      if (!partial) {
        this.load(box, 'all');
        Search.init(box);
      }
      Navigation.init(box, obj);
      if (obj) Navigation.go(box, $(obj));
      box.open(obj);
      return false;
    },

    destroy: function(box) {
      if (box.view !== 'fixed') $('.fkg-trigger').remove();
      box.container.remove();
    },

    configure: function(box) {
      box.loader = Utils.createElement('fkg-loader', null, 'div', Template.onceRender.loading);
      box.container = Utils.createElement('fkg-box', $(box.view === 'auto' ? 'body' : box.context), 'div', Template.getOnce(box, 'modal'), {'data-fkg-id': box.id, id: 'fkg' + box.id, 'data-fkg-context': box.view});
      Utils.getElement(_prefix + 'modal-body', box.container).html(Template.getOnce(box, 'layout'));

      const selectors = ['viewGraph', 'viewSearch', 'nav', 'back', 'breadcrumb', 'searchBox', 'searchHeader', 'searchList', 'facets', 'detailWrapper', 'detail', 'types', 'header', 'tabsWrapper', 'tabs', 'listWrapper', 'list', 'pagination', 'paginationList', 'paginationPrev', 'paginationNext', 'scroller', 'scrollerLeft', 'scrollerRight', 'snackbar'], len = selectors.length;
      for (var i = 0; i < len; i++) box[selectors[i]] = Utils.getElement('fkg-' + selectors[i], box.container);

      box.container.on('click', '[data-fkg-nav]', function(e) {
        return Navigation.go(box, $(this));
      });

      Dropdown.init(box);
      TabsScroller.init(box);

      if (box.view === 'auto') Modal.init(box);

      if (box.options.events) {
        $.each(box.options.events, function(eventName, func) {
          if (Events[eventName]) Events[eventName](box, func);
        });
      }
    },

    load: function(box, type) {
      if (type === 'all') Box.clean(box.detail, box.types, box.header, box.tabs, box.list);
      if (type === 'rel') Box.clean(box.header, box.tabs, box.list);
      if (type === 'all' || type === 'rel') {
        TabsScroller.hide(box);
        View.loaderShow(box, box.tabs.css('left', 'calc(50% - 3.6em)'));
      }
      Pagination.init(box);
    },

    loaderShow: function(box, $context) {
      $context.html(box.loader);
    },

    loaderHide: function(box, $context) {
     $context.find(box.loader).remove();
    },

    alert: function(box, type, status, msg) {
      const templ = Template.render(box, 'alert', {status: status, msg: msg, type: type});
      box.list.html(templ);
      box.searchList.html(templ);
    },

    error: function(box, status, msg) {
      this.alert(box, 'danger', status, msg + '. Please, try again later...');
      View.loaderHide(box, box.tabs);
    },

    notFound: function(box, msg) {
      this.alert(box, 'info', null, msg ? msg : 'No data found');
    },

    // Render views
    nodesButton: function(box, url, data) {
      $(box.context).attr({'data-fkg-nav': 'init', 'data-fkg-url': url, title: _title, style: 'cursor: pointer'}).on('click', function() { return View.init(box, this); });
      box.container.trigger('fkg:render', [box, url, data]);
    },

    nodesDetails: function(box, url, data, context) {
      box.viewGraph.show();
      box.viewSearch.hide();
      Box.fixedToFull(box);
      View.loaderShow(box, box.tabs);
      View.details(box, url, data, context);
      box.container.trigger('fkg:render', [box, url, data, context]);
    },

    nodesList: function(box, url, data) {
      box.viewGraph.show();
      box.viewSearch.hide();

      if (data.list.length === 1) {
        View.loaderShow(box, box.detail);
        Box.fullToFixed(box, 'detail');

        View.details(box, url, data);
      } else {
        View.loaderShow(box, box.list);
        Box.fullToFixed(box, 'list');

        const tmp = Navigation.current;
        Navigation.current = function(box) { return {_label: 'FKG list', _url: url} };
        Navigation.push(box);
        Navigation.current = tmp;

        const params = Url.getPathParams(url);
        box.header.html(data.total + ' Knowledge Graph links found ' + (params['query'] ? 'for ' + '<b>' + params['query'] + '</b>' : 'on this page') + ':');
        box.list.html(Template.render(box, 'list', {list: Data.convertToView(box, data.list, '_list')}));
      }

      Back.hide(box);
      box.container.trigger('fkg:render', [box, url, data]);
    },

    searchList: function(box, url, data, context) {
      box.viewGraph.hide();
      box.viewSearch.show();

      if (data.list && data.list.length) {
        box.searchHeader.html(Template.render(box, 'searchHeader', data.summary));
        box.searchList.html(Template.render(box, 'list', {list: Data.convertToView(box, data.list, '_list')}));
        box.facets.html(Template.render(box, 'facets', data.facets));
      } else {
        box.searchHeader.html('');
        box.searchList.html('');
        box.facets.html('');
        View.notFound(box);
      }
      Pagination.set(box, url, data.summary._total, 'search');
    },

    details: function(box, url, data, context) {
      if (!data.list.length) {
        View.notFound(box);
        View.loaderHide(box, box.tabs);
        return;
      }

      data = Data.convertToView(box, data.list, '_detail')[0];
      data['_shareUrl'] = Share.getUrl(box, data);
      if (data._url['typesLeaf'] || data._url['types']) Api.get(box, data._url['typesLeaf'] || data._url['types'], Data.processTypes).then(function(response) {
        View.typesList(response.box, response.url, response.data, context);
      }).catch(function(error) {
        Api.error(error);
      });
      box.detail.html(Template.render(box, 'detail', data));
      const longPathWrapper = box.detail.find('.fkg-long-path').parent();
      if (longPathWrapper && longPathWrapper.get(0) && longPathWrapper.get(0).nodeName === 'TD') box.detail.find('.fkg-long-path').parent().addClass('fkg-long-path');
    },

    typesList: function(box, url, data, context) {
      if (!data.length) {
        View.notFound(box, 'No related data found');
        View.loaderHide(box, box.tabs);
        return;
      }

      Api.get(box, context && context._type ? context._type : data[0]._url, Data.processRels).then(function(response) {
        View.tabsNav(response.box, response.url, response.data, context);
      }).catch(function(error) {
        Api.error(error);
      });
      box.types.html(Template.render(box, 'type', data));
      Types.set(box, context ? context._type : undefined);
    },

    tabsList: function(box, url, data, context) {
      const parts = Url.getPathParts(url, box.options.apiBase), type = parts[2], rels = parts[3], related = Data.groupNodes(box, Data.convertToView(box, data.list, '_list'), type, rels);
      Tabs.setData(box, context._id ? '#' + context._id : context._tab, {_url: url, _total: data.total});

      if (context._id) {
        box.list.append(Template.render(box, 'tabList', {list: related, _id: context._id}));
      } else { // list is paginating
        Tabs.getPanel(box, context._tab).html(Template.render(box, 'list', {list: related}));
        Pagination.set(box, url);
      }
    },

    tabsNav: function(box, url, data, context) {
      if (box.container.attr('data-fkg-context') != 'fixed') Box.fixedToFull(box);

      if (!context) context = {};
      View.loaderShow(box, box.list);
      if (data.length === 2 && data[0]._id === 'all-undirected') data.shift();
      box.tabs.html(Template.render(box, 'tab', data));
      TabsScroller.set(box);

      var promises = [];
      for (var i = 0, len = data.length; i < len; i++) {
        promises.push(Promise.all([Api.get(box, data[i]._url, Data.processNodes), Object({_id: data[i]._id}, context)]).then(function(response) {
          return View.tabsList(response[0].box, response[0].url, response[0].data, response[1]);
        }));
      }

      Promise.all(promises).then(function() {
        View.loaderHide(box, box.list);
        Tabs.set(box, context && context._tab ? context._tab : undefined);
      }).catch(function(error) {
        Api.error(error);
      });
    },
  };

  const Template = {
    modalWrapper: {
      full: _prefix + 'modal-dialog-centered'
    },
    listWrapper: {
      full: _prefix + 'col-12 ' + _prefix + 'col-sm-7 ' + _prefix + 'col-md-7 ' + _prefix + 'col-lg-7 ' + _prefix + 'pr-5',
      fix: _prefix + 'col-12 ' + _prefix + 'px-45'
    },
    detailWrapper: {
      full: _prefix + 'col-12 ' + _prefix + 'col-sm-5 ' + _prefix + 'col-md-5 ' + _prefix + 'col-lg-5',
      fix: _prefix + 'col-12'
    },
    onceRender: {
      modal: '<div class="' + _prefix + 'modal ' + _prefix + 'modal-{{view}}" tabindex="-1" role="dialog"><div class="' + _prefix + 'modal-dialog {{#if (eq view "auto")}}' + _prefix + 'modal-dialog-centered{{/if}}" role="document"><div class="' + _prefix + 'modal-content ' + _prefix + 'bg-light"><div class="' + _prefix + 'modal-body"></div></div><div class="fkg-snackbar ' + _prefix + 'snackbar ' + _prefix + 'alert ' + _prefix + 'alert-primary" role="alert"></div></div></div>',
      layout: '<nav class="' + _prefix + 'navbar ' + _prefix + 'sticky-top ' + _prefix + 'navbar-expand-lg ' + _prefix + 'navbar-dark ' + _prefix + 'bg-dark"><a class="' + _prefix + 'navbar-brand"><img src="{{options.apiBase}}s/resources-global/images/funnelback_logo.png"/><span>knowledge graph</span></a><button class="' + _prefix + 'navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation"><span class="' + _prefix + 'navbar-toggler-icon"></span></button><div class="' + _prefix + 'collapse ' + _prefix + 'navbar-collapse" id="navbarNav"><div class="' + _prefix + 'navbar-nav"><div class="fkg-back"></div><div class="fkg-breadcrumb"></div></div></div><form class="fkg-searchBox ' + _prefix + 'form-inline"><div class="' + _prefix + 'input-group"><input type="search" class="' + _prefix + 'form-control ' + _prefix + 'border-0 ' + _prefix + 'bg-dark ' + _prefix + 'text-white" placeholder="Discover with KnowledgeGraph" aria-describedby="button-search"><div class="' + _prefix + 'input-group-append"><button class="' + _prefix + 'btn ' + _prefix + 'btn-dark" type="button" data-fkg-nav="search"><span class="fa fa-fw fa-search"></span></button></div></div></form></nav><main><div class="' + _prefix + 'row fkg-viewGraph"><div class="fkg-detailWrapper ' + _prefix + 'col-12 ' + _prefix + 'col-sm-5 ' + _prefix + 'col-md-5 ' + _prefix + 'col-lg-5"><div class="' + _prefix + 'row ' + _prefix + 'no-gutters"><div class="' + _prefix + 'col-12 ' + _prefix + 'col-md-8 ' + _prefix + 'col-lg-9 ' + _prefix + 'modal-scroll ' + _prefix + 'bg-navy ' + _prefix + 'text-white"><section class="fkg-detail ' + _prefix + 'card ' + _prefix + 'h-100 ' + _prefix + 'border-0 ' + _prefix + 'rounded-0 ' + _prefix + 'bg-transparent"></section></div><div class="' + _prefix + 'col-12 ' + _prefix + 'col-md-4 ' + _prefix + 'col-lg-3 ' + _prefix + 'modal-scroll ' + _prefix + 'bg-white"><div class="fkg-types ' + _prefix + 'list-group ' + _prefix + 'list-group-sm ' + _prefix + 'list-group-flush"></div></div></div></div><div class="fkg-listWrapper ' + _prefix + 'col-12 ' + _prefix + 'col-sm-7 ' + _prefix + 'col-md-7 ' + _prefix + 'col-lg-7 ' + _prefix + 'pr-5"><section class="fkg-header ' + _prefix + 'mt-3 ' + _prefix + 'mb-4"></section><section class="fkg-tabsWrapper ' + _prefix + 'border-bottom ' + _prefix + 'mb-3"><div class="' + _prefix + 'nav-scroller"><a class="' + _prefix + 'scroller-nav ' + _prefix + 'scroller-left fkg-scrollerLeft ' + _prefix + 'btn ' + _prefix + 'py-0 ' + _prefix + 'px-2"><span class="fa fa-fw fa-chevron-left"></span></a><a class="' + _prefix + 'scroller-nav ' + _prefix + 'scroller-right fkg-scrollerRight ' + _prefix + 'btn ' + _prefix + 'py-0 ' + _prefix + 'px-2"><span class="fa fa-fw fa-chevron-right"></span></a><div class="' + _prefix + 'scroller-wrapper fkg-scroller"><div class="fkg-tabs ' + _prefix + 'nav ' + _prefix + 'nav-underscore" role="tablist"></div></div></div></section><div class="' + _prefix + 'list-scroll ' + _prefix + 'mt-1"><section class="fkg-list ' + _prefix + 'tab-content"></section>{{>pagination-block}}</div></div></div><div class="' + _prefix + 'row fkg-viewSearch"><div class="' + _prefix + 'col-12 ' + _prefix + 'col-md-3 ' + _prefix + 'col-lg-3 ' + _prefix + 'pl-5"><div class="fkg-facets ' + _prefix + 'search-scroll"></div></div><div class="' + _prefix + 'col-12 ' + _prefix + 'col-md-9 ' + _prefix + 'col-lg-9 ' + _prefix + 'pr-5"><div class="fkg-searchHeader ' + _prefix + 'border-bottom ' + _prefix + 'my-2 ' + _prefix + 'py-2 ' + _prefix + 'px-3"></div><div class="' + _prefix + 'search-scroll ' + _prefix + 'mt-1"><div class="fkg-searchList"></div>{{>pagination-block}}</div></div></div></main>',
      loading: '<div class="' + _prefix + 'text-navy ' + _prefix + 'text-center"><span class="fa fa-fw fa-2x fa-spinner fa-pulse"></span><span class="' + _prefix + 'text">Loading...</span></div>'
    },
    notpartial: {
      alert: '<div class="' + _prefix + 'alert ' + _prefix + 'alert-{{type}}" role="alert">{{#if status}}<h5>Error code: <b>{{status}}</b></h5>{{/if}}{{{msg}}}</div>',
      back: '{{> breadcrumbItem-block _nav="back" _title="Go back one page" _icon="fa fa-fw fa-arrow-left"}}',
      breadcrumb: '{{#each list}}{{#if @index}}{{#if _sub}}<div class="' + _prefix + 'btn-group ' + _prefix + 'ml-auto"><a class="' + _prefix + 'btn" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" title="{{_label}}">... <span class="' + _prefix + 'dropdown-toggle"></span></a><div class="' + _prefix + 'dropdown-menu ' + _prefix + 'bg-dark">{{#each _sub}}{{> breadcrumbItem-block _nav="breadcrumb" _label=_label _title=_label _url=_idx _classes="' + _prefix + 'dropdown-item"}}{{/each}}</div></div>{{else}}{{> breadcrumbItem-block _nav="breadcrumb" _label=_label _title=_label _url=_idx}}{{/if}}{{else}}{{> breadcrumbItem-block _nav="breadcrumb" _label=_label _title="Go to first page" _url=_idx _icon="fa fa-fw fa-home"}}{{/if}}{{/each}}',
      detail: '<div class="' + _prefix + 'card-body ' + _prefix + 'd-flex ' + _prefix + 'flex-column fkg-content" data-fkg-url="{{_url.self}}"><div class="' + _prefix + 'd-block"><span class="' + _prefix + 'd-inline-flex ' + _prefix + 'mb-2 ' + _prefix + 'align-items-center ' + _prefix + 'text-gray-light fkg-detail-type">{{>icon-block}}{{translate @root._translate.type _type}}</span>{{>img-block _classes="' + _prefix + 'float-right ' + _prefix + 'card-image-lg"}}<h4 class="fkg-title ' + _prefix + 'card-title">{{{_title}}}</h4><h5 class="fkg-subtitle ' + _prefix + 'card-subtitle ' + _prefix + 'text-gray-light">{{{_subtitle}}}</h5>{{> primary-block _classes="' + _prefix + 'mt-3 ' + _prefix + 'mb-4"}}<hr>{{#if secondary}}<table class="fkg-secondary-table ' + _prefix + 'table ' + _prefix + 'table-backgroundless ' + _prefix + 'table-borderless ' + _prefix + 'table-sm">{{#each secondary}}<tr><th class="' + _prefix + 'text-gray-light ' + _prefix + 'text-bold" scope="row">{{translate @root._translate.property ../_type key}}</th><td>{{> (item)}}</td></tr>{{/each}}</table>{{/if}}{{#if _desc}}<p class="' + _prefix + 'mb-4">{{_desc}}</p>{{/if}}</div><div class="' + _prefix + 'mt-auto">{{>url-block _classes="' + _prefix + 'btn ' + _prefix + 'btn-block ' + _prefix + 'btn-dark" _label="View" _icon="fa fa-fw fa-external-link"}}{{>phone-block _classes="' + _prefix + 'btn ' + _prefix + 'btn-block ' + _prefix + 'btn-dark" _label="Call now" _icon="icon-Phone"}}{{#if _shareUrl}}<a class="' + _prefix + 'btn ' + _prefix + 'btn-block ' + _prefix + 'btn-dark" href="#" data-fkg-nav="share" data-fkg-url="{{_shareUrl}}" title="Click to copy link to the clickboard">Share</a>{{/if}}</div></div>',
      facets: '{{#each this as |facet facetId|}}<div class="' + _prefix + 'card ' + _prefix + 'my-2 ' + _prefix + 'border-0 ' + _prefix + 'bg-transparent"><div class="' + _prefix + 'card-header ' + _prefix + 'bg-transparent ' + _prefix + 'py-2 ' + _prefix + 'px-3">{{facet.name}}{{#if facet._url}}<a href="" data-fkg-nav="facet" data-fkg-url="{{facet._url}}"><small class="' + _prefix + 'pl-1">{{facet._label}}</small></a>{{/if}}</div><div><div class="' + _prefix + 'card-body ' + _prefix + 'py-0 ' + _prefix + 'px-3"><ul class="' + _prefix + 'list-unstyled ' + _prefix + 'list-link ' + _prefix + 'mb-0 ' + _prefix + 'text-size-09 ' + _prefix + 'line-h-2">{{#each facet.allValues as |category|}}<li class="' + _prefix + 'my-1 {{#if (gte @index 8)}}' + _prefix + 'collapse collapsedFacet{{facetId}}{{/if}}"><a class="{{#if category.selected}}' + _prefix + 'active{{/if}}" href="#" data-fkg-nav="facet" data-fkg-url="{{category._url}}">{{#if (and category.selected (eq facet.guessedDisplayType "SINGLE_DRILL_DOWN") (ne @index 0))}}&#8627;{{/if}}{{category.label}}{{#unless category.selected}}<span class="' + _prefix + 'float-right ' + _prefix + 'text-bold">{{category.count}}</span>{{/unless}}</a></li>{{/each}}{{#if (gt facet.allValues.length 9)}}<li><a class="' + _prefix + 'collapsed" data-fkg-nav="collapse" data-toggle="collapse" data-target="collapsedFacet{{facetId}}" href="#" role="button" aria-expanded="false"><span class="fa fa-fw fa-chevron-up"></span></a></li>{{/if}}</ul></div></div></div>{{/each}}',
      list: '{{>list-block}}',
      tab: '{{#each this}}{{>tabItem-block}}{{/each}}',
      tabList: '<div class="' + _prefix + 'tab-pane" id="{{_id}}" role="tabpanel" aria-labelledby="{{_id}}-tab">{{>list-block}}</div>',
      type: '{{#each this}}{{>typeItem-block}}{{/each}}',
      listHeader: '<h3 class="' + _prefix + 'text-bold">{{title}}</h3><p class="' + _prefix + 'text-truncate">{{desc}}</p>',
      searchHeader: 'Showing results <strong>{{_start}}</strong>-<strong>{{_end}}</strong> of <strong>{{_total}}</strong> for <em>{{_query}}</em>',
      paginationItem: '<span class="' + _prefix + 'page-item{{#if _isActive}} ' + _prefix + 'active{{/if}}"><a class="' + _prefix + 'page-link" href="#" data-fkg-nav="page" data-fkg-url="{{_url}}">{{_label}}</a></span>'
    },
    partial: {
      text: '{{val}}',
      date: '{{#dateFormat @root._options.dateFormat}}{{val}}{{/dateFormat}}',
      badge: '<span class="' + _prefix + 'badge ' + _prefix + 'badge-{{key}} ' + _prefix + 'badge-{{badgeType val}}">{{translate @root._translate.type _type key}}: {{val}}</span>',
      email: '{{#if val}}<a class="{{_classes}}" href="mailto:{{val}}">{{>icon-block _classes=""}}{{#if _label}}{{_label}}{{else}}{{val}}{{/if}}</a>{{/if}}',
      path: '<span class="fkg-long-path">{{val}}</span>',
      phone: '{{#if val}}<a class="{{_classes}}" href="tel:{{val}}">{{>icon-block _classes="" _icon=_icon}}{{#if _label}}{{_label}}{{else}}{{val}}{{/if}}</a>{{/if}}',
      url: '{{#if _viewUrl}}<a class="{{_classes}}" href="#" data-fkg-nav="external" data-fkg-url="{{_viewUrl}}" target="_blank">{{>icon-block _classes=""}}{{#if _label}}{{_label}}{{else}}{{_viewUrl}}{{/if}}</a>{{/if}}',
      icon: '{{#if _icon}}<span class="{{_icon}} {{_classes}}"></span> {{/if}}',
      img: '{{#if _img}}<img class="' + _prefix + 'rounded-circle {{_classes}}" src="{{_img}}" />{{/if}}',
      primary: '{{#if primary}}<div class="{{_classes}}">{{#each primary}}<span class="' + _prefix + 'mr-2">{{> (item)}}</span>{{/each}}</div>{{/if}}',
      breadcrumbItem: '<a class="{{#if _classes}}{{_classes}}{{else}}' + _prefix + 'nav-item ' + _prefix + 'nav-link{{/if}} ' + _prefix + 'text-truncate" href="#" data-fkg-nav="{{_nav}}" data-fkg-url="{{_url}}" title="{{_title}}">{{>icon-block}}{{{_label}}}</a>',
      list: '<div class="' + _prefix + 'card-list ' + _prefix + 'mb-2">{{#each list}}{{#if group}}{{>listGroup-block}}{{else}}{{>listItem-block collapseId=@index}}{{/if}}{{/each}}</div>',
      listGroup: '<div class="' + _prefix + 'card-group ' + _prefix + 'pt-3 ' + _prefix + 'pb-2 ' + _prefix + 'px-3">{{group}}</div>',
      listItem: '<div class="' + _prefix + 'card ' + _prefix + 'py-1"><div class="' + _prefix + 'd-flex ' + _prefix + 'align-items-center ' + _prefix + 'my-2"><a class="' + _prefix + 'align-self-center ' + _prefix + 'px-2 ' + _prefix + 'py-3 ' + _prefix + 'mr-2 ' + _prefix + 'border-right ' + _prefix + 'collapsed {{#unless secondary}}' + _prefix + 'invisible{{/unless}}" data-fkg-nav="collapse" data-toggle="collapse" href="#collapsed{{_id}}{{collapseId}}" role="button" aria-expanded="false" aria-controls="{{collapseId}}"><span class="fa fa-fw fa-chevron-up"></span></a>{{>img-block _classes="' + _prefix + 'card-image ' + _prefix + 'ml-2 ' + _prefix + 'mr-3"}}<div class="' + _prefix + 'd-flex ' + _prefix + 'flex-fill ' + _prefix + 'align-items-center"><div class="' + _prefix + 'd-flex ' + _prefix + 'flex-column{{#if primary}} ' + _prefix + 'col-7{{/if}}">{{#if _viewUrl}}{{>url-block _classes="" _label=_title _icon=""}}{{else}}{{{_title}}}{{/if}}<span class="' + _prefix + 'd-inline-flex ' + _prefix + 'text-muted">{{{_subtitle}}}</span></div>{{>primary-block}}</div><a href="#" data-fkg-nav="{{#if _nav}}{{_nav}}{{else}}go{{/if}}" data-fkg-url="{{_url.self}}" class="' + _prefix + 'card-link ' + _prefix + 'btn"><span class="fa fa-fw fa-arrow-circle-right"></span></a></div>{{>listItemSecondary-block}}</div>',
      listItemMenu: '<div class="' + _prefix + 'btn-group ' + _prefix + 'ml-auto"><a class="' + _prefix + 'btn" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="fa fa-fw fa-ellipsis-v"></span></a><div class="' + _prefix + 'dropdown-menu ' + _prefix + 'dropdown-menu-right">{{>email-block _classes="' + _prefix + 'dropdown-item" _label="Send email" _icon="fa fa-fw fa-envelope-o"}}{{>phone-block _classes="' + _prefix + 'dropdown-item" _label="Call now" _icon="icon-Phone"}}{{>url-block _classes="' + _prefix + 'dropdown-item" _label="Visit now" _icon="fa fa-fw fa-external-link"}}</div></div>',
      listItemMenuItem: '<a class="' + _prefix + 'dropdown-item" href="{{url}}"><span class="fa fa-fw fa-{{icon}}"></span> {{label}}</a>',
      listItemSecondary: '{{#if secondary}}<div id="collapsed{{_id}}{{collapseId}}" class="' + _prefix + 'collapse fkg-secondary"><div class="' + _prefix + 'd-flex ' + _prefix + 'flex-wrap ' + _prefix + 'py-2 ' + _prefix + 'ml-475 ' + _prefix + 'mr-2 ' + _prefix + 'border-top">{{#each secondary}}<span class="' + _prefix + 'd-flex ' + _prefix + 'flex-fill ' + _prefix + 'mt-1 ' + _prefix + 'mb-2"><span class="' + _prefix + 'text-bold ' + _prefix + 'mr-1">{{translate @root._translate.property ../_type key}}</span><span class="' + _prefix + 'd-inline-block ' + _prefix + 'text-width ' + _prefix + 'text-muted ' + _prefix + 'text-truncate" title="{{val}}">{{>(item) _type=../_type}}</span></span>{{/each}}</div></div>{{/if}}',
      pagination: '<nav class="fkg-pagination ' + _prefix + 'my-3 ' + _prefix + 'pagination ' + _prefix + 'justify-content-center"><span class="fkg-paginationPrev ' + _prefix + 'page-item ' + _prefix + 'pagination-nav ' + _prefix + 'disabled"><a class="' + _prefix + 'page-link" href="#" data-fkg-nav="prev" tabindex="-1"><span class="fa fa-fw fa-chevron-left"></span> Prev</a></span><span class="fkg-paginationList ' + _prefix + 'pagination-pages ' + _prefix + 'd-flex"></span><span class="fkg-paginationNext ' + _prefix + 'page-item ' + _prefix + 'pagination-nav"><a class="' + _prefix + 'page-link" href="#" data-fkg-nav="next">Next <span class="fa fa-fw fa-chevron-right"></span></a></span></nav>',
      tabItem: '<a class="' + _prefix + 'nav-link" id="{{_id}}-tab" href="#{{_id}}" data-fkg-nav="tab" data-fkg-url="{{_url}}" data-fkg-total="{{_total}}" data-toggle="tab" role="tab" aria-controls="{{_id}}" aria-selected="false">{{translate @root._translate.relationship _label}} <small class="' + _prefix + 'text-muted">{{_count}}</small></a>',
      typeItem: '<a href="#" class="' + _prefix + 'list-group-item ' + _prefix + 'd-flex ' + _prefix + 'pb-4 ' + _prefix + 'flex-wrap ' + _prefix + 'flex-column ' + _prefix + 'justify-content-center ' + _prefix + 'align-items-center ' + _prefix + 'text-center" data-fkg-nav="rel" data-fkg-url="{{_url}}" data-fkg-label="{{translate @root._translate.type _label}}"><small class="' + _prefix + 'badge ' + _prefix + 'badge-gray-light ' + _prefix + 'badge-pill ' + _prefix + 'ml-auto ' + _prefix + 'mb-2">{{_count}}</small>{{>icon-block _classes="' + _prefix + 'mb-1 ' + _prefix + 'text-gray-light"}}{{translate @root._translate.type _label}}</a>'
    },

    _default: '_defaultTemplate',

    urlPath: 's/knowledge-graph/templates.json',

    configure: function(box) {
      $.map(Template.notpartial, function(template, name) {
        return box.template[name] = HandlebarsUtil.compile(template);
      });
      Template.setFields(box, Template._default, {});

      const url = Url.get(this.urlPath, {collection: box.options.collection, profile: box.options.profile}, box.options.apiBase);
      return Api.get(box, url).then(function(response) {
        if (response.data) {
          var templ;
          for (var type in response.data) {
            templ = $.isString(response.data[type]) ? response.data[response.data[type]] : response.data[type];
            Template.setFields(box, type, templ);
            Template.setGrouping(box, type, templ.sort || '');
            Template.setIcon(box, type, templ.icon || '');
            Template.setSorting(box, type, templ.sort || '');
          }
        }
      }).catch(function(error) {
        Api.error(error, true);
      });
    },

    get: function() {
      const args = [].slice.call(arguments), box = args.shift();
      var templ = box.template, i, len;
      for (i = 0, len = args.length; i < len; i++) {
        templ = templ[args[i]] || templ[Template._default];
      }
      return templ;
    },

    getOnce: function(box, name) {
      const template = HandlebarsUtil.compile(Template.onceRender[name]);
      return template(box);
    },

    isBadgeField: function(field) {
      return Model._badges.indexOf(field) > -1;
    },

    isDateField: function(field) {
      return field.indexOf('date_') > -1 || field.indexOf('_date') > -1;
    },

    isEmailField: function(val) {
      return /\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*/.test(val);
    },

    isImageField: function(field) {
      return field.indexOf('photo') > -1 || field.indexOf('image') > -1 || field.indexOf('img') > -1;
    },

    isPathField: function(field) {
      return field.indexOf('path') > -1;
    },

    isPhoneField: function(field) {
      return field.indexOf('phone') > -1 || field.indexOf('mobile') > -1;
    },

    render: function(box, name, data) {
      data._options = box.options;
      data._translate = Translation.keys;
      return box.template[name](data);
    },

    setFields: function(box, type, config) {
      const fields = Object.keys(Model._fields), len = fields.length;
      var fieldName;
      for (var i = 0; i < len; i++) {
        fieldName = '_' + fields[i];
        if (!box.template.fields[fieldName]) box.template.fields[fieldName] = {};
        box.template.fields[fieldName][type] = config[fields[i]] || Model._fields[fields[i]];
      }
    },

    setGrouping: function(box, type, config) {
      box.template.group[type] = config.field ? config.field : '';
    },

    setIcon: function(box, type, config) {
      box.template.icon[type] = (box.options.iconPrefix || '') + config;
    },

    setSorting: function(box, type, config) {
      box.template.sort[type] = (config.field && config.order) ? (config.order === 'DESC' ? '-' : '') + config.field : '';
    }
  }

  /**
   * Handle navigation
   */
  const Navigation = {
    go: function(box, $trigger, skip) {
      if (!$.isDefined(skip)) skip = false;
      var type = $trigger.attr('data-fkg-nav'), url = $trigger.attr('data-fkg-url');

      if (type === 'back') {
        Navigation.back(box, $trigger);
        return false;
      }
      if (type === 'collapse') {
        Collapse.toggle(box, $trigger);
        return false;
      }
      if (type === 'external') {
        $trigger.trigger(Events.name('nav'), [box, url, type]);
        $trigger.attr('href', url);
        return true;
      }
      if (type === 'share') {
        $trigger.trigger(Events.name('nav'), [box, url, type]);
        Share.copyToClipboard(box, url);
        return false;
      }
      if (type === 'tab') {
        $trigger.trigger(Events.name('nav'), [box, url, type]);
        Pagination.init(box);
        Navigation.push(box);
        Tabs.toggle(box, $trigger);
        return false;
      }

      if (box.container.attr('data-fkg-context') === 'fixed') {
        Box.openAutoFromFixed(box, $trigger);
        return;
      }

      var proccessCallback = Data.processNodes, renderCallback = View.nodesDetails, context = {}, reloadAll = false;
      if (type.indexOf('init') < 0 && !skip) {
        Navigation.push(box);
      }
      if (type === 'breadcrumb') {
        const idx = parseInt(url);
        Breadcrumb.update(box, idx);
        url = box.stackUrl[idx]._url;
        if (Search.isUrl(box, url)) {
          type = 'facet';
        } else if (!Url.isNodeDetail(url)) {
          renderCallback = View.nodesList;
        }
        reloadAll = true;
      }
      if (type.indexOf('go') > -1) {
        Breadcrumb.update(box);
        reloadAll = true;
      }
      if (type === 'home') {
        url = Navigation.head(box)._url;
        reloadAll = true;
      }
      if (type === 'next' || type === 'page' || type === 'prev') {
        box.results.url = url;
        if (box.results.model === 'graph') {
          renderCallback = View.tabsList;
        } else {
          proccessCallback = Data.processSearch;
          renderCallback = View.searchList;
        }
        context = this.current(box);
      }
      if (type === 'facet') {
        box.results.url = url;
        proccessCallback = Data.processSearch;
        renderCallback = View.searchList;
      }
      if (type === 'search') {
        Breadcrumb.update(box);
        url = Url.get(url, Object.assign(box.options.searchParams || {}, {collection: box.options.collection, profile: box.options.profile, query: $trigger.closest('form').find('input[type="search"]').val() || ''}));
        box.results.url = url;
        proccessCallback = Data.processSearch;
        renderCallback = View.searchList;
      }
      if (type === 'rel') {
        View.load(box, 'rel');
        proccessCallback = Data.processRels;
        renderCallback = View.tabsNav;
        Types.set(box, url);
      }

      $trigger.trigger(Events.name('nav'), [box, url, type]);
      if (reloadAll) View.load(box, 'all');

      Api.get(box, url, proccessCallback).then(function(response) {
        renderCallback(response.box, response.url, response.data, context);
      }).catch(function(error) {
        Api.error(error);
      });
      return false;
    },

    back: function(box, $trigger) {
      const current = Navigation.current(box), last = Navigation.pop(box);
      Back.update(box);
      if (!box.stackUrl.length) Breadcrumb.hide(box);
      if (!last) return;

      if (last._url != current._url) {
        $trigger.attr({'data-fkg-nav': last._model === 'search' ? 'facet' : 'go', 'data-fkg-url': last._url});
        Navigation.go(box, $trigger, true);
        Breadcrumb.update(box, box.stackUrl.length);
      } else if (last._type != current._type) {
        $trigger.attr({'data-fkg-nav': 'rel', 'data-fkg-url': last._type});
        Navigation.go(box, $trigger, true);
      } else if (last._tab != current._tab) {
        Tabs.set(box, last._tab);
        $trigger.trigger(Events.name('nav'), [box, Tabs.getUrl(box), 'tab']);
      } else if (last._page != current._page) {
        $trigger.attr({'data-fkg-nav': 'page', 'data-fkg-url': last._page});
        Navigation.go(box, $trigger, true);
      }
    },

    current: function(box) {
      return box.viewSearch.is(':visible') ? Search.current(box) : {
        _label: Utils.getDetailsTitle(box),
        _url: Utils.getDetailsUrl(box),
        _type: Types.getUrl(box),
        _tab: Tabs.getUrl(box),
        _page: Pagination.currentPage(box),
        _model: 'graph',
      };
    },

    init: function(box, obj) {
      box.stackUrl = obj && $(obj).attr('data-fkg-nav') === 'init-go' && box.stackUrl.length ? [box.stackUrl[0]] : [];
      Breadcrumb.update(box);
    },

    head: function(box) {
      if (!box.stackUrl.length) return undefined;
      const first = box.stackUrl[0];
      return first;
    },

    peek: function(box) {
      return box.stackUrl[box.stackUrl.length - 1];
    },

    pop: function(box) {
      if (!box.stackUrl.length) return undefined;
      const last = box.stackUrl.pop();
      return last;
    },

    push: function(box) {
      const current = Navigation.current(box), last = Navigation.peek(box);
      if (last != current && current._url) {
        box.stackUrl.push(current);
        Back.update(box);
      }
    }
  };

  const Pagination = {
    currentPage: function(box) {
      return box.results.url;
    },

    hide: function(box) {
      box.pagination.hide();
    },

    init: function(box) {
      box.results.total = null;
      this.hide(box);
    },

    set: function(box, url, total,  model) {
      if (model) box.results.model = model;
      if (url) box.results.url = url;
      if ($.isDefined(total)) box.results.total = parseInt(total);
      if (box.results.total > box.results.size) {
        this.show(box);
        this.update(box);
      } else {
        this.hide(box);
      }
    },

    show: function(box) {
      Utils.showElement(box.pagination);
    },

    update: function(box) {
      const current = parseInt(Url.getPathParams(box.results.url)[Model[box.results.model].pagination.start] || 1), prev = current - box.results.size;
      if (prev > -1) {
        box.paginationPrev.removeClass(_prefix + 'disabled');
        box.paginationPrev.find('a').attr('data-fkg-url', this.url(box, prev)).removeAttr('disabled');
      } else {
        box.paginationPrev.addClass(_prefix + 'disabled');
        box.paginationPrev.find('a').attr('disabled', 'disabled');
      }

      const next = current + box.results.size;
      if (next < box.results.total) {
        box.paginationNext.removeClass(_prefix + 'disabled');
        box.paginationNext.find('a').attr('data-fkg-url', this.url(box, next)).removeAttr('disabled');
      } else {
        box.paginationNext.addClass(_prefix + 'disabled');
        box.paginationNext.find('a').attr('disabled', 'disabled');
      }

      if (box.options.maxPagination) {
        box.paginationList.html('');
        doPaging((current - 1) / box.results.size, Math.ceil(box.results.total / box.results.size), box.options.maxPagination);

        function doPaging(current, pages, range, start) {
          if (!$.isDefined(start)) start = 0;
          if (pages < range) range = pages;
          var i = Math.min(pages + start - range, Math.max(start, current - (range / 2 | 0)));
          const end = i + range;
          for (; i < end; i++) {
            box.paginationList.append(Template.render(box, 'paginationItem', {_label: i + 1, _isActive: current === i, _url: Pagination.url(box, i * box.results.size + 1)}));
          }
        }
      }
    },

    url: function(box, start) {
      const currentUrl = box.results.url;
      var params = Url.getPathParams(currentUrl);
      params[Model[box.results.model].pagination.size] = box.results.size;
      params[Model[box.results.model].pagination.start] = start;
      return Url.get(Url.getPathParts(currentUrl).join('/'), params);
    }
  };

  /**
   * Handle behaviour of DOM elements
   */
  const Back = {
    update: function(box) {
      box.back.html(box.stackUrl.length ? Template.render(box, 'back', {}) : '');
    },

    hide: function(box) {
      box.back.html('');
    }
  }

  const Breadcrumb = {
    update: function(box, idx) {
      if ($.isDefined(idx)) box.stackBreadcrumb = box.stackBreadcrumb.slice(0, box.stackBreadcrumb.indexOf(idx));
      else if (box.stackUrl.length) {
        if (box.stackBreadcrumb.indexOf(box.stackUrl.length - 1) < 0) box.stackBreadcrumb.push(box.stackUrl.length - 1);
      } else box.stackBreadcrumb = [];

      const l = box.stackBreadcrumb.length, m = Math.floor(box.options.maxBreadcrumb / 2);
      box.breadcrumb.html(Template.render(box, 'breadcrumb', {list: l > box.options.maxBreadcrumb ? Array.prototype.concat(get(0, m), [{_label: 'More', _sub: get(m, l - m - 1)}], get(l - m - 1, l)) : get(0, l)}));

      function get(start, end) {
        var data = [], tmp;
        for (; start < end; start++) {
          tmp = box.stackUrl[box.stackBreadcrumb[start]];
          if (tmp) {
            tmp['_idx'] = box.stackBreadcrumb[start];
            data.push(tmp);
          }
        }
        return data;
      }
    },

    hide: function(box) {
      box.breadcrumb.html('');
    }
  };

  const Collapse = {
    selector: '[data-toggle="collapse"]',
    active: _prefix + 'show',
    collapse: _prefix + 'collapse',
    collapsed: _prefix + 'collapsed',

    toggle: function(box, $trigger) {
      $trigger.hasClass(this.collapsed) ? this.show(box, $trigger) : this.hide(box, $trigger);
    },

    show: function(box, $trigger) {
      if (!$trigger.hasClass(this.collapsed)) return;

      box.container.find(this.selector).each(function() {
        if (($trigger.attr('href') && $(this).attr('href') === $trigger.attr('href')) || ($trigger.attr('data-target') && $(this).attr('data-target') === $trigger.attr('data-target'))) $(this).removeClass(Collapse.collapsed).attr('aria-expanded', true);
      });
      box.container.find('.' + this.collapse).each(function() {
        if ('#' + $(this).attr('id') === $trigger.attr('href') || $(this).hasClass($trigger.attr('data-target'))) $(this).addClass(Collapse.active);
      });
    },

    hide: function(box, $trigger) {
      if ($trigger.hasClass(this.collapsed)) return;

      box.container.find(this.selector).each(function() {
        if (($trigger.attr('href') && $(this).attr('href') === $trigger.attr('href')) || ($trigger.attr('data-target') && $(this).attr('data-target') === $trigger.attr('data-target'))) $(this).addClass(Collapse.collapsed).attr('aria-expanded', false);
      });
      box.container.find('.' + this.collapse).each(function() {
        if ('#' + $(this).attr('id') === $trigger.attr('href') || $(this).hasClass($trigger.attr('data-target'))) $(this).removeClass(Collapse.active);
      });
    },
  };

  const Dropdown = {
    selector: '[data-toggle="dropdown"]',
    active: _prefix + 'show',
    menu:  _prefix + 'dropdown-menu',

    init: function(box) {
      box.container.on('click', this.selector, function(e) {
        e.preventDefault();
        e.stopPropagation();
        Dropdown.toggle(box, $(this));
      })
    },

    toggle: function(box, $trigger) {
      const $menu = $trigger.parent().find('.' + this.menu);
      if (!$menu.length) return;
      if ($menu.hasClass(this.active)) {
        $trigger.attr('aria-expanded', false);
        this.hide(box, $menu);
      } else {
        $trigger.attr('aria-expanded', true).focus();
        this.show(box, $menu);
      }
    },

    show: function(box, $trigger) {
      if ($trigger.hasClass(this.active)) return;
      box.container.find('.' + this.menu).removeClass(this.active);
      $trigger.addClass(this.active);
      Utils.getElement(Modal.selector, box.container).on('mouseup', function(e) {
        if(e.target !== $trigger) Dropdown.hide(box, $trigger);
      });
    },

    hide: function(box, $trigger) {
      if (!$trigger.hasClass(this.active)) return;
      $trigger.removeClass(this.active);
    },
  };

  const Modal = {
    backdrop: _prefix + 'modal-backdrop',
    backdropShow: _prefix + 'show',
    opened: _prefix + 'modal-open',
    selector: _prefix + 'modal',
    eventSuffix: 'fkg-modal',

    init: function(box) {
      Utils.getElement(this.selector, box.container).on('mouseup.' + this.eventSuffix, function(e) {
        if(e.target === this) box.close();
      });

      $(document).on('keydown.' + this.eventSuffix, function(e) {
        const code = e.keyCode || e.which;
        if (code == 27) box.close(); // 27 - code for escape key
      });
    },

    destroy: function(box) {
      Utils.getElement(this.selector, box.container).off('mouseup.' + this.eventSuffix);
      $(document).off('keydown.' + this.eventSuffix);
    },

    show: function(box) {
      if (box.isOpen) return;
      Utils.getElement(this.selector, box.container).show(400, function() {
        Modal.showBackdrop(box);
        box.isOpen = true;
        $(this).attr('aria-hidden', false);
        if (box.options.modalFocus) $(this).focus();
      });
    },

    hide: function(box) {
      if (!box.isOpen) return;
      Utils.getElement(this.selector, box.container).hide(400, function() {
        Modal.hideBackdrop(box);
        box.isOpen = false;
        $(this).attr('aria-hidden', true);
      });
    },

    showBackdrop: function(box) {
      if(!box.backdrop) box.backdrop = Utils.createElement(Modal.backdrop, null, 'div', null, {class: Modal.backdropShow});
      if (box.options.modalBackdrop) $('body').addClass(Modal.opened).append(box.backdrop);
    },

    hideBackdrop: function(box) {
      $('body').removeClass(Modal.opened);
      box.backdrop.remove();
    }
  };

  const ProgressBar = {
    start: function() {
      NProgress.start();
    },

    stop: function() {
      NProgress.done();
    }
  };

  const Search = {
    eventSuffix: 'fkg-search',

    init: function(box) {
      box.viewSearch.hide();
      if (!box.options.searchUrl) box.searchBox.hide();
      else {
        box.searchBox.find('[type="button"]').attr('data-fkg-url', box.options.searchUrl);
        box.searchBox.find('input').on('focus', function() {
          $(document).on('keydown.' + Search.eventSuffix, function(e) {
            if ((e.keyCode || e.which) === 13) { // enter
              box.searchBox.find('[type="button"]').click();
              return false;
            }
          });
        }).on('focusout', function() {
          $(document).off('keydown.' + Search.eventSuffix);
        });
      }
    },

    current: function(box) {
      const url = Pagination.currentPage(box);
      return {
        _label: 'Search for ' + box.searchHeader.find('em').text(),
        _url: url,
        _type: undefined,
        _tab: undefined,
        _page: url,
        _model: 'search',
      };
    },

    getUrl: function(box, path, params) {
      return Url.get(box.options.searchUrl + path, params);
    },

    isUrl: function(box, url) {
      return url.match(box.options.searchUrl) ? true : false;
    }
  }

  const Share = {
    urlPath: 's/knowledge-graph/index.html',

    copyToClipboard: function(box, link) {
      const el = document.createElement('textarea'), msg = 'Link was copied to the clickboard';
      el.value = link;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      box.snackbar? Snackbar.show(box, msg) : alert(msg);
    },

    getUrl: function(box, data) {
      const id = data['_fields'] && data['_fields']['id'] && data['_fields']['id'][0];
      return id ? Url.get(this.urlPath, {collection: box.options.collection, profile: box.options.profile, targetUrl: id}, box.options.apiBase) : undefined;
    }
  }

  const Snackbar = {
    selector: '[role="alert"]',
    active: _prefix + 'show',

    show: function(box, msg) {
      if (box.snackbar.hasClass(this.active)) return;
      box.snackbar.text(msg || '').addClass(this.active);
      setTimeout(function() { Snackbar.hide(box) }, 3000); // 3 secs
    },

    hide: function(box) {
      if (!box.snackbar.hasClass(this.active)) return;
      box.snackbar.removeClass(this.active);
    },
  }

  const Tabs = {
    selector: '[role="tab"]',
    active: _prefix + 'active',

    toggle: function(box, $trigger) {
      this.hide(box);
      this.show(box, $trigger);
    },

    show: function(box, $trigger) {
      if ($trigger.hasClass(this.active)) return;

      $trigger.addClass(this.active);
      box.list.find($trigger.attr('href')).addClass(this.active).show();
      Pagination.set(box, $trigger.attr('data-fkg-url'), $trigger.attr('data-fkg-total'), 'graph');
    },

    hide: function(box) {
      Pagination.hide(box);
      box.tabs.find('.' + this.active).removeClass(this.active);
      box.list.find('.' + this.active).removeClass(this.active).hide();
    },

    get: function(box) {
      return Utils.getElement(this.active, box.tabs);
    },

    getLabel: function(box) {
      return Utils.getElementLabel(this.active, box.tabs);
    },

    getUrl: function(box) {
      return this.get(box).attr('href');
    },

    getPanel: function(box, url) {
      return box.list.find(url);
    },

    set: function(box, url) {
      const sel = url ? '[href="' + url + '"]' : this.selector + ':first-child';
      this.toggle(box, box.tabs.find(sel));
    },

    setData: function(box, url, data) {
      box.tabs.find('[href="' + url + '"]').attr({'data-fkg-url': data._url, 'data-fkg-total': data._total});
    }
  };

  const TabsScroller = {
    active: _prefix + 'show',

    init: function(box) {
      box.scrollerRight.on('click', function(e) {
        e.stopPropagation();
        e.preventDefault();
        $(this).addClass(_prefix + 'disabled').attr('disabled', 'disabled');
        TabsScroller.show(box, box.scrollerLeft);
        box.tabs.animate({left: '-=' + TabsScroller.moveRight(box)}, 500, function() {
          box.scrollerRight.removeClass(_prefix + 'disabled').removeAttr('disabled');
          if (!Math.round(TabsScroller.getHiddenListWidth(box))) TabsScroller.hide(box, box.scrollerRight);
        });
      });

      box.scrollerLeft.on('click', function(e) {
        e.stopPropagation();
        e.preventDefault();
        $(this).addClass(_prefix + 'disabled').attr('disabled', 'disabled');
        TabsScroller.show(box, box.scrollerRight);
        box.tabs.animate({left: '+=' + TabsScroller.moveLeft(box)}, 500, function() {
          box.scrollerLeft.removeClass(_prefix + 'disabled').removeAttr('disabled');
          if (!TabsScroller.getListPosition(box).left) TabsScroller.hide(box, box.scrollerLeft);
        });
      });
    },

    readjust: function(box) {
      const leftPos = this.getListPosition(box).left;

      if (this.getVisibleListWidth(box) < this.getListWidth(box)) this.show(box, box.scrollerRight);
      else this.hide(box, box.scrollerRight);

      if (leftPos < 0) this.show(box, box.scrollerLeft);
      else {
        this.hide(box, box.scrollerLeft);
        box.tabs.animate({left: '-=' + leftPos + 'px'}, 'slow');
      }
    },

    set: function(box) {
      box.tabs.css('left', 0);
      this.readjust(box);
    },

    show: function(box, $trigger) {
      if ($trigger) {
        if ($trigger.hasClass(this.active)) return;
        $trigger.addClass(this.active);
      } else {
        this.show(box, box.scrollerLeft);
        this.show(box, box.scrollerRight);
      }
    },

    hide: function(box, $trigger) {
      if ($trigger) {
        if (!$trigger.hasClass(this.active)) return;
        $trigger.removeClass(this.active);
      } else {
        this.hide(box, box.scrollerLeft);
        this.hide(box, box.scrollerRight);
      }
    },

    moveLeft: function(box) {
      const abs = Math.abs(TabsScroller.getListPosition(box).left);
      return  abs < this.getVisibleListWidth(box) ? abs : this.getVisibleListWidth(box);
    },

    moveRight: function(box) {
      const abs = Math.abs(this.getHiddenListWidth(box));
      return  abs < this.getVisibleListWidth(box) ? abs : this.getVisibleListWidth(box);
    },

    getVisibleListWidth: function(box) {
      return box.scroller.outerWidth();
    },

    getHiddenListWidth: function(box) {
      return this.getVisibleListWidth(box) - this.getListWidth(box) - this.getListPosition(box).left;
    },

    getListWidth: function(box) {
      var itemsWidth = 0;
      box.tabs.find('.' + _prefix + 'nav-link').each(function() {
        itemsWidth += $(this).outerWidth();
      });
      return itemsWidth;
    },

    getListPosition: function(box) {
      return box.tabs.position();
    },
  };

  const Types = {
    selector: '[data-fkg-nav="rel"]',
    active: _prefix + 'active',

    get: function(box) {
      return Utils.getElement(this.active, box.types);
    },

    getLabel: function(box) {
      return Utils.getElementLabel(this.active, box.types);
    },

    getUrl: function(box) {
      return Utils.getElementUrl(this.active, box.types);
    },

    set: function(box, url) {
      const sel = url ? '[data-fkg-url="' + url + '"]' : this.selector + ':first-child';
      this.get(box).removeClass(this.active);
      box.types.find(sel).addClass(this.active);
      box.header.html(Template.render(box, 'listHeader', {title: Types.getLabel(box), desc: 'Discover ' + Types.getLabel(box).toLowerCase() + ' related to "' + Utils.getDetailsTitle(box) + '"'}));
    }
  };

  /**
   * Helpers
   */

  // Dates helper (use moment.js)
  const Dates = {
    format: function(date, format) {
      var formattedDate;
      try { formattedDate = moment(date, 'YYYY-MM-DD kk:mm').format(format); }
      catch(e) { formattedDate = date; }
      return formattedDate;
    },
    range: function(date) {
      var year = moment(date).year, currentYear = moment().year();
      return year > currentYear + 1 || year < currentYear - 1 ? year : moment(date).fromNow();
    }
  };

  const Log = {
    log: function(a1, a2, a3) {
      this.factory('log', a1, a2, a3);
    },

    warn: function(a1, a2, a3) {
      this.factory('warn', a1, a2, a3);
    },

    factory: function(type, a1, a2, a3) {
      console.group(_title);
      console[type](a1 || '', a2 || '', a3 || '');
      console.groupEnd();
    }
  };

  // URL helper
  const Url = {
    encode: function(params) {
      return encodeURIComponent(params);
    },

    get: function(path, params, base) {
      return (base ? base : '') + path + (params ? '?' + $.param(params) : '');
    },

    getPathParams: function(url, base) {
      const paramsQuery = Url.path(url, base).split('?')[1], params = paramsQuery ? paramsQuery.split('&') : [];
      var i, len, pathParams = {};
      for (i = 0, len = params.length; i < len; i++) {
        const pair = params[i].split('=', 2);
        pathParams[pair[0]] = pair.length == 1 ? '' : decodeURIComponent(pair[1].replace(/\+/g, ' '));
      }
      return pathParams;
    },

    getPathParts: function(url, base, index) {
      const parts = Url.path(url, base).split('?')[0].split('/') || [];
      return index && parts[index] ? parts[index] : parts;
    },

    getUrl: function(url, base) {
      return Url.path(url, base).split('?')[0];
    },

    isNodeDetail: function(url) {
      return url.match(/\/nodes\/[0-9]+/g) ? true : false;
    },

    path: function(url, base) {
      return base ? url.slice(base.length) : url;
    },

    setBase: function(str) {
      return !str ? '' : (str.endsWithIndexOf('/') ? str : str + '/');
    },

    storageKey: function(url, base) {
      const delimiter = '', pathParams = Url.getPathParams(url);
      return Url.getPathParts(url, base).join(delimiter) + Object.keys(pathParams).map(function(key) { return key + pathParams[key]; }).join(delimiter);
    },
  };

  // Helpers to work with DOM and native JS methods
  const Utils = {
    getElement: function(id, $context) {
      return ($context || $('body')).find('.' + id);
    },

    getElementLabel: function(id, $context) {
      const $element = Utils.getElement(id, $context);
      return $element.attr('data-fkg-label') ? $element.attr('data-fkg-label') : $element.text();
    },

    getElementUrl: function(id, $context) {
      const $element = Utils.getElement(id, $context);
      return $element.attr('data-fkg-url') ? $element.attr('data-fkg-url') : $element.attr('href');
    },

    createElement: function(id, $context, tag, content, attrs, events, forced) {
      if (!tag) tag = 'div';
      if (!forced) forced = false;

      var $element = Utils.getElement(id, $context);
      if (!$element.length || forced) {
        $element = $('<' + tag + '>').attr('class', id);
        if (attrs) {
          if (attrs['class']) {
            $element.addClass(attrs['class']);
            delete attrs['class'];
          }
          $element.attr(attrs);
        }
        if (content) $element.html(content);
        if (events) $.each(events, function(eventName, func) { $element.on(eventName, func); });
        if ($context) $context.append($element);
      }
      return $element;
    },

    showElement: function($context) {
      $context.show().css('display', 'flex');
    },

    // -----
    getDetailsTitle: function(box) {
      return Utils.getElementLabel('fkg-title', box.detail);
    },

    getDetailsUrl: function(box) {
      return Utils.getElementUrl('fkg-content', box.detail);
    }
  };

  // Translations
  const Translation = {
    currentLang: window.navigator.languages ? window.navigator.languages[0] : window.navigator.language,
    defaultLang: 'en',
    storageKey: 'translations',
    urlPath: 's/knowledge-graph/labels.json',
    keys: {
      property: 'property',
      relationship: 'relationship',
      type: 'type',
    },

    configure: function(box) {
      const url = Url.get(this.urlPath, {collection: box.options.collection, profile: box.options.profile}, box.options.apiBase);
      return Api.get(box, url).then(function(response) {
        Storage.setItem(Translation.storageKey, response.data);
      }).catch(function(error) {
        Api.error(error, true);
      });
    },

    get: function() {
      if (!arguments.length) {
        return undefined;
      } else {
        var keys = [].slice.call(arguments), translation, translations = Storage.getItem(this.storageKey);
        if (translations) {
          translation = (translations[keys[0]] && translations[keys[0]][keys[1]]) || undefined;
          if (keys[0] === Translation.keys.property && translation) {
            while($.isString(translation) && translations[keys[0]][translation]) translation = translations[keys[0]][translation];
            if ($.isObject(translation) && translation[keys[2]]) translation = translation[keys[2]];
          }
        }
        return translation ? translation : keys[keys.length-1].capitalize();
      }
    }
  };

  // Handlebars
  const HandlebarsUtil = {
    compile: function(template) {
      return KnowledgeGraph.Handlebars.compile(template);
    },

    registerPartial: function(templates) {
      $.map(templates, function(template, name) {
        return KnowledgeGraph.Handlebars.registerPartial(name + '-block', template);
      });
    },
  };

  HandlebarsUtil.registerPartial(Template.partial);

  KnowledgeGraph.Handlebars.registerHelper({
    badgeType: function(val) {
      return val.toLowerCase().replace(/ /g, '-');
    },
    dateFormat: function(format, options) {
      return Dates.format(options.fn(this), format);
    },
    item: function() {
      var templ = 'text';
      if (Template.isBadgeField(this.key)) templ = 'badge';
      if (Template.isDateField(this.key)) templ = 'date';
      if (Template.isEmailField(this.val)) templ = 'email';
      if (Template.isPathField(this.key)) templ = 'path';
      if (Template.isPhoneField(this.key)) templ = 'phone';
      return templ + '-block';
    },
    translate: function() {
      const args = [].slice.call(arguments);
      var i, len = args.length, keys = [];
      for (i = 0; i < len; i++) if ($.isString(args[i])) keys.push(args[i]);
      return Translation.get.apply(Translation, keys);
    },
    eq: function (v1, v2) {
      return v1 === v2;
    },
    ne: function (v1, v2) {
        return v1 !== v2;
    },
    lt: function (v1, v2) {
        return v1 < v2;
    },
    gt: function (v1, v2) {
        return v1 > v2;
    },
    lte: function (v1, v2) {
        return v1 <= v2;
    },
    gte: function (v1, v2) {
        return v1 >= v2;
    },
    and: function () {
        return Array.prototype.slice.call(arguments).every(Boolean);
    },
    or: function () {
        return Array.prototype.slice.call(arguments, 0, -1).some(Boolean);
    },
    not: function(v) {
      return !v;
    }
  });

  // Generate plugin
  function Plugin() {
    var args = [].slice.call(arguments), option = args.shift();

    return this.each(function () {
      var $this = $(this),
        data    = $this.data('flb.knowledgeGraph'),
        options = $.extend(true, {}, KnowledgeGraph.defaults, data || {}, $.isObject(option) && option);

      if (!data && /destroy|hide/.test(option)) return;
      if (!data) $this.data('flb.knowledgeGraph', (data = new KnowledgeGraph(this, options)));
      if ($.isString(option) && $.isFunction(data[option])) data[option].apply($this, args);
    });
  };

  $.fn.knowledgeGraph             = Plugin;
  $.fn.knowledgeGraph.Constructor = KnowledgeGraph;

  $.isDefined = function(obj) { return typeof(obj) !== 'undefined'; }
  $.isObject  = function(obj) { return typeof(obj) === 'object'; }
  $.isString  = function(obj) { return typeof(obj) === 'string'; }
  $.dataVals  = function(obj, key) { var parts = key.split('.'), key = parts.shift(); if (parts.length) { for (var i = 0, len = parts.length; i < len; i++) { obj = obj[key] || {}; key = parts[i]; } } return obj[key]; }

  String.prototype.capitalize = function() { return this.charAt(0).toUpperCase() + this.slice(1); }
  String.prototype.endsWithIndexOf = function(suffix) { return this.indexOf(suffix, this.length - suffix.length) !== -1; }

  return KnowledgeGraph;
}($));