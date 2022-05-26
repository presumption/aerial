module OverviewPage exposing (..)

import Filters exposing (Filter(..))
import Header exposing (view)
import Html exposing (Html)
import Html.Attributes as HA
import Html.Events
import Report exposing (..)


type Msg
    = FilterBy Filter


viewHeader app =
    Header.view [ Html.a [ HA.href "/" ] [ Html.text app ] ]


viewContent components journeys =
    Html.div [ HA.class "components" ] <|
        List.map viewComponent components
            ++ [ viewJourneys journeys ]


viewComponent : Component -> Html Msg
viewComponent { component, features } =
    if List.isEmpty features then
        Html.div
            [ HA.class "component-empty" ]
        <|
            [ Html.div
                [ HA.class "component-name" ]
                [ Html.text component ]
            , Html.div
                [ HA.class "component-content" ]
                [ Html.text "No features found!" ]
            ]

    else
        Html.div
            [ HA.class "component" ]
        <|
            [ Html.button
                [ HA.class "component-name"
                , Html.Events.onClick (FilterBy <| ComponentNameEquals component)
                , HA.href <| "/component/" ++ component
                ]
                [ Html.text component ]
            , Html.div
                [ HA.class "component-content" ]
                (List.map (viewFeature component) features)
            ]


viewFeature : String -> String -> Html msg
viewFeature component feature =
    Html.a
        [ HA.class "feature"
        , HA.href <| "/component/" ++ component ++ "#" ++ feature
        ]
        [ Html.text feature ]


viewJourneys : List Journey -> Html Msg
viewJourneys journeys =
    Html.div [ HA.class "journeys" ] <|
        [ Html.button
            [ HA.class "journeys-title"
            , Html.Events.onClick (FilterBy <| Journeys)
            ]
            [ Html.text "Journeys" ]
        , Html.div [ HA.class "journeys-content" ] <|
            List.map viewJourney journeys
        ]


viewJourney : Journey -> Html Msg
viewJourney journey =
    Html.button
        [ HA.class "journey"
        , Html.Events.onClick (FilterBy <| Journeys)
        ]
        [ Html.text journey.name ]
