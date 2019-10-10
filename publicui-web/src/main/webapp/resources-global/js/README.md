## Minification of Javascript files

To minify Javascript files any existed library can be used as long that library is supporting ECMAScript 6.

One of the recommended library is `terser`, it can be found here https://www.npmjs.com/package/terser.

### Usage

```
terser [input file] -o [output file]
```

### Example
```
terser  publicui-web/src/main/webapp/resources-global/js/funnelback.session-cart-0.1.js -o publicui-web/src/main/webapp/resources-global/js/funnelback.session-cart-0.1.min.js
```
