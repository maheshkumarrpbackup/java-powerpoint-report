/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/find-pages',
    'find/app/page/find-search',
    'find/app/configuration',
    'i18n!find/nls/bundle'
], function(FindPages, FindSearch, AboutPage, configuration, i18n) {
    return FindPages.extend({

        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch,
                    icon: 'fa fa-search',
                    pageName: 'search',
                    title: i18n['app.search']
                }
            ];

            if (!configuration().hod) {
                this.pages.push({
                    constructor: AboutPage,
                    icon: 'fa fa-cog',
                    pageName: 'about',
                    title: i18n['app.about']
                })
            }
        }
    });
});
