/*
    Editor   : Asad Shahabuddin
    Created  : Jul 18, 2015
    Reference: Calaca - Search UI for Elasticsearch.
               https://github.com/romansanchez/Calaca
               http://romansanchez.me
               @rooomansanchez
*/

/* Calaca Controller
 * On change in search box, search() will be called, and results are bind to scope as results[].
*/
Calaca.controller('calacaCtrl', ['calacaService', '$scope', '$location', function(results, $scope, $location)
{
    /* Constant(s). */
    SHORT_DESC_LEN = 512;
    ASSESSOR_ID    = "Asad_Shahabuddin";

    /* Scope variables. */
    $scope.results = [];
    $scope.offset = 0;
    $scope.evalCount = 0;

    /* Global variables. */
    var relevance = {};
    var paginationTriggered;
    var maxResultsSize = CALACA_CONFIGS.size;
    var searchTimeout;

    /*
    Download a file.
    Note: HTML5 ready browsers only.
    */
    var download = function(fileName, text)
    {
        var pom = document.createElement("a");
        pom.setAttribute("href", "data:text/plain;charset=utf-8," + encodeURIComponent(text));
        pom.setAttribute("download", fileName);

        if(document.createEvent)
        {
            var event = document.createEvent("MouseEvents");
            event.initEvent("click", true, true);
            pom.dispatchEvent(event);
        }
        else
        {
            pom.click();
        }
    };

    $scope.delayedSearch = function(mode)
    {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function()
        {
            $scope.search(mode)
        }, CALACA_CONFIGS.search_delay);
    }

    /* On search, reinitialize array, then perform search and load results. */
    $scope.search = function(m)
    {
        $scope.results = [];
        $scope.offset = m == 0 ? 0 : $scope.offset;//Clear offset if new query
        $scope.loading = m == 0 ? false : true;//Reset loading flag if new query

        if(m == -1 && paginationTriggered)
        {
            if ($scope.offset - maxResultsSize >= 0 ) $scope.offset -= maxResultsSize;
        }     
        if(m == 1 && paginationTriggered)
        {
            $scope.offset += maxResultsSize;
        }
        $scope.paginationLowerBound = $scope.offset + 1;
        $scope.paginationUpperBound = ($scope.offset == 0) ? maxResultsSize : $scope.offset + maxResultsSize;
        $scope.loadResults(m);
    };

    /* Load search results into array. */
    $scope.loadResults = function(m)
    {
        results.search($scope.query, m, $scope.offset).then(function(a)
        {
            /* Load results. */
            var i = 0;
            for(;i < a.hits.length; i++)
            {
                $scope.results.push(a.hits[i]);
            }

            /* Set time took. */
            $scope.timeTook = a.timeTook;

            /* Set total number of hits that matched query. */
            $scope.hits = a.hitsCount;

            /* Pluralization. */
            $scope.resultsLabel = ($scope.hits != 1) ? "results" : "result";

            /* Check if pagination is triggered. */
            paginationTriggered = $scope.hits > maxResultsSize ? true : false;

            /* Set loading flag if pagination has been triggered. */
            if(paginationTriggered)
            {
                $scope.loading = true;
            }
        });
    };

    $scope.paginationEnabled = function()
    {
        return paginationTriggered ? true : false;
    };

    /* Filter the result's title. */
    $scope.filter = function(obj)
    {
        var res = obj.title;
        if(typeof res == "undefined")
        {
            res = obj.docno;
        }
        else if(res.charAt(0) == '>')
        {
            res = res.substring(1, res.length);
        }
        return res.replace(/\?+/g, "(...)");
    };

    /* Clip text to 512 characters. */
    $scope.abstract = function(obj)
    {
        if(typeof obj.text == "undefined")
        {
            return "";
        }
        var len = (obj.text.length < SHORT_DESC_LEN) ? obj.text.length : SHORT_DESC_LEN;
        var suffix = len < SHORT_DESC_LEN ? "" : "...";
        return obj.text.substring(0, len) + suffix;
    };

    /* Update the relevance score for the specified key. */
    $scope.updateRelevance = function(key, val)
    {
        if(!relevance.hasOwnProperty(key.docno))
        {
            $scope.evalCount++;
        }
        relevance[key.docno] = val;
    };

    /* Output the relevance scores to the file system. */
    $scope.write = function(qid)
    {
        if(typeof qid == "undefined" || qid.length == 0)
        {
            alert("Query ID cannot be blank.")
        }
        else
        {
            var text = "";
            for(var key in relevance)
            {
                text += qid + " " + ASSESSOR_ID + " " + key + " " + relevance[key] + "\n";
            }
            console.log("%c" + text, "font-family: Courier New;");
            download("qrels-" + qid + ".txt", text);
        }
    };
}]);
/* End of controllers.js */