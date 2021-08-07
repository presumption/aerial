module Header exposing (..)

import Html
import Html.Attributes as HA
import Theme


viewHeader breadcrumbs headerContent =
    Html.div [ HA.class "page-header" ] <|
        [ Html.div [ HA.class "page-title" ]
            [ Html.div [ HA.class "breadcrumbs" ] breadcrumbs
            , Theme.viewSwitcher
            ]
        ]
            ++ headerContent
