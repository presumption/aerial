module Header exposing (..)

import Html
import Html.Attributes as HA
import Theme


view breadcrumbs =
    Html.div [ HA.class "page-header" ] <|
        [ Html.div [ HA.class "page-title" ]
            [ Html.div [ HA.class "breadcrumbs" ] breadcrumbs
            , Theme.viewSwitcher
            ]
        ]
            ++ [ Html.div [ HA.class "filters" ] [ Html.text "Filters" ] ]
