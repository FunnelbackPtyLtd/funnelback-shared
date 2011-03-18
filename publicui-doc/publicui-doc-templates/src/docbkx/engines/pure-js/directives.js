'ol#fb-results li.result' : {
  // '<-' is used to declare an iteration
  'result<-SearchTransaction.response.resultPacket.results' : {
    'h3 span.fb-filetype' : 'result.fileType',
    'h3 a@href' : 'result.displayUrl',
    'h3 a' : 'result.title',

    'p span.fb-summary' : 'result.summary',

    // The following one maps a function to a directive.
    // It's the only way to extend the engine
    'p span.fb-date' : function (arg) {
      return new Date(arg.item.date).toUTCString();
    },

    'cite' : 'result.displayUrl',
    'a.fb-cached@href' : 'result.cacheUrl'
  }
}