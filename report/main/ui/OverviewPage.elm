module OverviewPage exposing (viewOverviewPage)

import Header exposing (viewHeader)
import Html exposing (Html)
import Html.Attributes as HA
import Report exposing (..)
import UI exposing (Msg)


viewOverviewPage : String -> List Component -> Html Msg
viewOverviewPage app components =
    Html.div
        [ HA.class "overview-page" ]
    <|
        [ viewSimpleHeader app
        , Html.div [ HA.class "components" ] (List.map viewComponent components)
        ]


viewSimpleHeader app =
    viewHeader
        [ Html.text app ]
        []


viewComponent : Component -> Html msg
viewComponent component =
    if List.isEmpty component.features then
        Html.div
            [ HA.class "component-empty" ]
        <|
            [ Html.div
                [ HA.class "component-name" ]
                [ Html.text component.component ]
            , Html.div
                [ HA.class "component-features" ]
                [ Html.text "No features found!" ]
            ]

    else
        Html.div
            [ HA.class "component" ]
        <|
            [ Html.a
                [ HA.class "component-name"
                , HA.href <| "/component/" ++ component.component
                ]
                [ Html.text component.component ]
            , Html.div
                [ HA.class "component-features" ]
                (List.map (viewFeature component.component) component.features)
            ]


viewFeature : String -> String -> Html msg
viewFeature component feature =
    Html.a
        [ HA.class "feature"
        , HA.href <| "/component/" ++ component ++ "#" ++ feature
        ]
        [ Html.text feature ]
