#Requirejs常用配置和应用

requirejs、require方法冲突

如果加载了多个requirejs脚本，每个requirejs会判断是否浏览器已经实现了require和define方法。如果浏览器已经自带require和define方法，或者之前已经有一个requirejs脚本执行，那么这个requirejs就会立刻停止执行。所以，即使页面上加载了多次requirejs脚本也不会有什么问题。

配置Context

我把context叫做一个命名空间，因为每一个context都有一个名字，这样同名而功能不同的模块就可以放在不同的context中以防冲突。

如果开发人员没有配置context，那么，requirejs还会生成一个默认的context，这个默认的context配置大致如下：

requirejs.config({

    context: "_",  // default context name

    baseUrl: "./",

    waitSeconds：7, // how long canloading last

    paths: {},

    bundles: {},

    pkgs: {},

    shim: {},

    config: {}

});

注意：在不指定context名称的情况下，任何配置和调用都是针对默认context的修改和调用。

空间名称 – context

如果requirejs初始化时自定义配置context，那么默认创建的context的name 就是”_”。如果需要添加新的context，只需指定一个新的contextName即可，比如下面这个调用就会创建一个新的context：

requirejs({context:”new content name”});

同名的context只会有一个，配置同名的context等于修改这个context的属性。

加载超时 – waitSeconds

每个context都配置了一个加载超时的时间，某个模块如果没有初始化，加载的时间又超过了这个时间，就会被认为加载失败。

加载超时是针对整个context下的所有模块而言的，而不是单指某个模块，也就是说这个默认的7秒是指所有模块应该在7秒之内全部加载完成。7秒之后，如果有没有被加载的模块，将抛出error指示哪些模块没有加载。（requirejs每隔50毫秒做一次判断）。

基准URL – baseUrl

每个context的基准URL默认值是”./”。

第一个context的基准URL

如果开发人员没有指定context名称，那么这个第一个context就是requirejs默认生成的context，否则就是开发人员自己定义的context。在不指定基准URL的前提下，第一个context的基准URL设定比较特殊，除了标准的设定方法（参考后面的基准URL标准设定方法），还可以使用以下两种特殊方式设置：

第一种：通过requirejs或require对象配置

在确认requirejs脚本之前没有其它requirejs执行过的前提下：

<script>requirejs={baseUrl: './'}</script>

<script data-main="scripts/app.js"src="../require.js"></script>

或

<script>require={baseUrl: './'}</script>

<script data-main="scripts/app.js"src="../require.js"></script>

注意：通过这种方式设置基准URL，data-main指定的脚本文件位置也会变成相对于基准URL的路径，因为data-main指定的脚本本身只是依赖的关系之一。而且，data-main指定的脚本也属于第一个context。

比如下面这种情况：

<script>requirejs={baseUrl: 'scripts/lib'}</script>

<script data-main="scripts/app.js"src="../require.js"></script>

脚本模块app.js的最终路径变成了"scripts/lib/scripts/app.js"，不是原来的"scripts/app.js"，而且它的依赖名称也会变为scripts/lib/scripts/app（requirejs默认会去掉脚本路径的最后一个“.js”，除非data-main的值以“/”开头，或包含“:”，或包含“?”）。

第二种：根据script元素的data-main属性指定的脚本路径计算

如果没有设定baseUrl，requirejs会根据script元素data-main属性指定的JavaScript文件路径计算出一个基准URL。

比如data-main="scripts/app.js"，那么baseUrl就是"scripts/"：

<script data-main="scripts/app.js"src="../require.js"></script>

基准URL标准设定方法

除了第一个context可以使用上面的方法，其它自定义的context配置baseUrl就只能使用下面这两种方法。但这两种方法同样也可以用来修改第一个context的属性，在不指定context名称的情况下，其实就是修改第一个context。

通过requirejs或require方法（这两个本身就是同一个方法）设置

以下两个等于把默认命名空间的基准URL设置成了scripts/lib：

requirejs({baseUrl:'scripts/lib'});

require({baseUrl:'scripts/lib'});

通过requirejs.config方法

requirejs.config({baseUrl:'scripts/lib'});

require.config({baseUrl:'scripts/lib'});

其实config方法调用的就是requirejs方法，所以它们是一样的。

模块依赖 – deps

模块依赖是指个模块之间的相互依赖关系，脚本运行时，只有当依赖的模块全部加载完成之后，当前脚本才会执行，这就是依赖关系的作用。

依赖关系使用数组配置，数组元素为字符串（即模块的名称），一般是相对于baseUrl的路径，只不过没有文件后缀。而且，为了比较方便的获取模块入口，模块一般会通过define方法定义。因为，通过define定义的模块，可以被依赖数组后面的回调函数直接获取并使用。

以jQuery为例，在jQuery脚本的末尾一般有下面两行代码：

if(typeof define === "function"&& define.amd && define.amd.jQuery) {

    define("jquery", [], function () { return jQuery; } );

}

再以underscore为例，在脚本末尾有下面几行代码：

if (typeof define === 'function' && define.amd) {

  define('underscore', [],function() {

    return _;

  });

}

模块和模块位置

使用require配置依赖模块的时候，只是声明了模块的名称，却不知道模块的具体位置。在没有特殊声明的情况下，requirejs认为模块名和文件名相同，因此，只要两者一致，requirejs就可以正确找到脚本文件。但如果不同，就需要通过path配置：

requirejs.config({

    baseUrl:"scripts/lib",

    paths: {

        jquery:'jquery-1.7.2'

    }

});

这样，requirejs就知道jquery模块位于scripts/lib/jquery-1.7.2.js文件中。

第一个context的依赖关系

<script>requirejs={deps: ['jquery']}</script>

<script data-main="scripts/app.js"src="../require.js"></script>

其它context的依赖关系

与基准URL的方式一样，既可以通过requirejs方法，也可通过requirejs.config方法配置。

模块束-bundles

如果一个JS文件中有多个模块，就可以使用模块束的方式声明：

requirejs.config({

    baseUrl:"scripts/lib",

    bundles: {

        jsUtil:['MathUtil', 'DateUtil']

    }

});

上面这个例子就是说在scripts/lib/jsUtils.js文件中有MathUtil和DateUtil这两个子模块。

JS包– packages

如果一个文件夹中有多个JS文件，使用path的方式写全就需要很多行代码，这个时候如果使用包的方式声明就可以省去很多麻烦：

requirejs.config({

    baseUrl:"scripts/lib",

    pkgs: [{name:'jqueryui',location: 'jqueryui/',main: 'core'}]

});

这样定义之后，凡是在scripts/lib/jqueryui/目录下的模块就可以通过这种方式正确找到：

require(['jqueryui/button', 'jqueryui/dialog']);

上面这个例子就是获取scripts/lib/jqueryui/button.js和scripts/lib/jqueryui/dialog.js的例子。另外，因为jqueryui是一个目录，并不对应一个JS文件，所以又有一个main属性，这个属性一般对应这个JS包中的主程序文件。上面的例子中，jqueryui的主程序就在scripts/lib/jqueryui/core.js中。

楔子 – shim

并不是所有的JS模块都会像jquery和underscore那样调用define方法定义自己，这样requirejs就不知道你这个模块的入口在哪，该通过哪个对象来调用这个模块，特别是那些早版本的JS模块，因为那是还没有define和require的概念。

requirejs.config({

    baseUrl:"scripts/lib",

    shim: {jquery: {deps:[],exportsFn: func, exports：'jQuery',init:func}}

});

虽然模块没有使用define方法定义自己，但开发人员应该是知道如何获取文件中的模块的，所以，requirejs提供了两种方式让开发人员把模块对象返回给requirejs管理：

在exportsFn或init方法中设置，然后作为返回值；
使用exports设置，比如”a.b.c”，那requirejs就知道通过window.a.b.c可以获取。
映射 – Map

先来看问题：一些第三方JS插件的依赖关系是事先设定好的，不太好修改依赖模块的名称，而如果某个模块有多个版本或有其他模块和它同名，则使用上面的配置都无法解决问题。比如path只是解决模块名称到路径的问题，而这个面对的是切换模块名称的问题。于是requirejs提出了映射的概念，根据当前脚本的名称动态修改所依赖模块的ID，是它指向正确的模块。

假如在你的硬盘下有以下几个模块：

foo1.0.js
foo1.2.js
some/newmodule.js
some/oldmodule.js
在newmodule.js和oldmodule.js中都有require(‘foo’)调用，要解决冲突只需要这样配置即可：

requirejs.config({

    map: {

        'some/newmodule':{

            'foo': 'foo1.2'

        },

        'some/oldmodule':{

            'foo':'foo1.0'

        }

    }

});

主程序入口data-main

<script data-main="scripts/app.js"src="../require.js"></script>

不管页面上有多少个script元素有data-main属性，requirejs只认最后一个script元素的data-main属性，忽略其他script元素的data-main属性。

Requirejs获取data-main属性之后，并没有立即执行data-main指定的脚本文件（因为这个脚本文件可能还依赖了其他模块），而是把它作为了一个被依赖的模块，加入到第一个context的依赖数组中。比如下面这种情况就是把scripts/app这个模块加到一个名叫linus的context中：

<script type="text/javascript">

    requirejs={

        context: 'linus',

        baseUrl:"./",

        skipDataMain: false

    };

</script>

<script data-main="scripts/app.js"src="../require.js"></script>

全局配置

忽略script元素的data-main

在浏览器中，有一个选项叫skipDataMain，可以让requirejs忽略script元素的data-main。在默认情况下，requirejs成功加载之后，会立马查找页面上所有script元素，并且把最后一个有data-main属性的script元素的data-main最为主程序入口。

<script>requirejs={skipDataMain:true}</script>

<scriptdata-main="scripts/app.js"src="../require.js"></script>

模块定义 – define(name, deps, callback)

你会发现define方法没有指定context名称，这是因为define方法只调用于被依赖的模块中，而require方法已经为依赖的模块指定了context名称，所以，这个模块被哪个context需要，它就属于那一个context。

参数name是模块的名称，deps是该模块所依赖的其他模块的名称，callback一般返回该模块的实际可被使用对象。比如jQuery的模块定义回调函数返回的就是jQuery对象。

Error

加载Error

这种error就是浏览器自带的Error对象，只不过requirejs给它附加了其他属性。

message的格式为：msg + '\nhttp://requirejs.org/docs/errors.html#' + id。
error.requireType就是就是message后面的id；
error.requireModules一般指需要加载却没加载成功的模块名称；
error.originalError是指发生其他错误导致模块加载失败的原始error对象。