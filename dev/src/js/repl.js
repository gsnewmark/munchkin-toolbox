goog.addDependency('base.js', ['goog'], []);

goog.addDependency('js/cljs/core.js', ['cljs.core'],
                   ['goog.string', 'goog.array', 'goog.object',
                    'goog.string.StringBuffer']);

goog.addDependency('js/clojure/browser/event.js',
                   ['clojure.browser.event'],
                   ['cljs.core', 'goog.events.EventType', 'goog.events.EventTarget', 'goog.events']);

goog.addDependency('js/clojure/browser/net.js',
                   ['clojure.browser.net'],
                   ['goog.net.xpc.CrossPageChannel', 'clojure.browser.event', 'goog.net.xpc.CfgFields',
                    'cljs.core', 'goog.net.EventType', 'goog.json', 'goog.net.XhrIo']);

goog.addDependency('js/weasel/impls/websocket.js',
                   ['weasel.impls.websocket'],
                   ['clojure.browser.event', 'clojure.browser.net', 'cljs.core',
                    'goog.net.WebSocket']);

goog.addDependency('js/cljs/reader.js', ['cljs.reader'], ['cljs.core', 'goog.string']);

goog.addDependency('js/weasel/repl.js', ['weasel.repl'],
                   ['clojure.browser.event', 'clojure.browser.net', 'cljs.core',
                    'weasel.impls.websocket', 'cljs.reader']);

goog.addDependency('js/ws_repl.js', ['ws_repl'], ['cljs.core', 'weasel.repl']);

goog.require('ws_repl');
