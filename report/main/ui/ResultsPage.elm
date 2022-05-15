module ResultsPage exposing (..)

import Dict exposing (Dict)
import Header
import Html exposing (Html)
import Html.Attributes as HA
import Html.Events
import Report exposing (..)
import Set exposing (Set)


type Msg
    = ToggleExampleExpanded String


viewHeader app component =
    Header.view
        [ Html.a [ HA.href "/" ] [ Html.text app ]
        , Html.text " / "
        , Html.text component
        ]


viewFeature : List Example -> Set String -> String -> Html Msg
viewFeature examples expanded feature =
    Html.div
        [ HA.class "feature" ]
    <|
        [ Html.div [ HA.class "feature-name" ]
            [ Html.a [ HA.href <| "#" ++ feature ] [ Html.text "§ " ]
            , Html.span [] [ Html.text feature ]
            ]
        , viewExamples expanded <| examplesForFeature feature examples
        ]


viewExamples : Set String -> List Example -> Html Msg
viewExamples expanded examples =
    let
        groups =
            groupExamplesByVariables examples
    in
    Html.div [ HA.class "examples" ] <|
        Dict.foldl (viewGroups expanded) [] groups


viewGroups : Set String -> List String -> List Example -> List (Html Msg) -> List (Html Msg)
viewGroups expanded vars examples acc =
    if List.isEmpty vars then
        List.map
            (\example -> viewExampleAsListItem (Set.member example.example expanded) example)
            examples

    else
        Html.table
            [ HA.class "group" ]
            [ Html.thead [] [ Html.tr [] <| List.map viewVariableKey vars ++ [ Html.th [] [ Html.text "Example" ] ] ]
            , Html.tbody [] <|
                List.map
                    (\example -> viewExampleAsTableItem (Set.member example.example expanded) example)
                    examples
            ]
            :: acc


viewExampleAsListItem : Bool -> Example -> Html Msg
viewExampleAsListItem expanded example =
    Html.div [ HA.class "example-list-item" ] [ viewExample expanded example ]


viewExampleAsTableItem : Bool -> Example -> Html Msg
viewExampleAsTableItem expanded example =
    let
        vars =
            variableValues example
    in
    Html.tr [] <|
        List.map viewVariableValue vars
            ++ [ Html.td [ HA.class "example-table-item" ] [ viewExample expanded example ] ]


viewExample : Bool -> Example -> Html Msg
viewExample expanded ({ example, type_, locations } as e) =
    Html.div
        [ HA.class "example" ]
    <|
        [ Html.div
            [ HA.class "example-name"
            , Html.Events.onClick (ToggleExampleExpanded example)
            ]
            ([ Html.text example ] ++ viewBadges e)
        ]
            ++ viewDetails expanded locations


viewDetails : Bool -> List Loc -> List (Html msg)
viewDetails expanded locations =
    if expanded then
        [ Html.div
            [ HA.class "details" ]
          <|
            List.map
                (\loc -> Html.div [] [ Html.text loc.file, Html.text ":", Html.text <| String.fromInt loc.line ])
                locations
        ]

    else
        []


viewBadges : Example -> List (Html msg)
viewBadges example =
    let
        exampleTypeBadge =
            case example.type_ of
                Todo ->
                    [ viewTodoBadge ]

                HowTo ->
                    [ viewHowToBadge ]

                Normal ->
                    []

        tags =
            List.map viewTagBadge example.tags
    in
    exampleTypeBadge ++ tags


viewHowToBadge =
    Html.div [ HA.class "how-to-badge" ] [ Html.text "how-to" ]


viewTodoBadge =
    Html.div [ HA.class "todo-badge" ] [ Html.text "todo" ]


viewTagBadge tag =
    Html.div [ HA.class "tag-badge" ] [ Html.text tag ]


viewVariableKey key =
    Html.th [] [ Html.text key ]


viewVariableValue key =
    Html.td [] [ Html.text key ]


examplesForFeature feature examples =
    List.filter (.feature >> (==) feature) examples


viewComponentNotFoundPage component =
    Html.text <| "Component \"" ++ component ++ "\" not found"


type alias Groups =
    Dict (List String) (List Example)


groupExamplesByVariables : List Example -> Groups
groupExamplesByVariables examples =
    List.foldl group Dict.empty examples


group : Example -> Groups -> Groups
group example acc =
    let
        vars =
            variableKeys example

        examples =
            Dict.get vars acc |> Maybe.withDefault []
    in
    Dict.insert vars (example :: examples) acc


variableKeys : Example -> List String
variableKeys example =
    example.variables
        |> Dict.keys
        |> List.sort


variableValues : Example -> List String
variableValues example =
    example.variables
        |> Dict.toList
        |> List.sort
        |> List.map dropFirst


dropFirst : ( a, a ) -> a
dropFirst ( _, second ) =
    second
