module Aerial exposing (main)

import Browser exposing (Document, UrlRequest(..))
import Filters exposing (Filter(..), Filters)
import Html exposing (Html)
import Html.Attributes as HA
import Json.Decode as Decode
import OverviewPage exposing (Msg(..), viewComponent)
import Report exposing (..)
import ResultsPage exposing (Msg(..), viewComponentNotFoundPage)
import Set exposing (Set)
import Theme exposing (Msg(..), Theme(..))


main : Program Flags Model Msg
main =
    Browser.element
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }


type alias Model =
    { theme : Theme
    , report : Report
    , filters : Filters
    , ui : UI
    }


type Msg
    = ThemeMsg Theme.Msg
    | OverviewPageMsg OverviewPage.Msg
    | ResultsPageMsg ResultsPage.Msg


type alias Flags =
    { report : Decode.Value }


init : Flags -> ( Model, Cmd Msg )
init flags =
    let
        report : Report
        report =
            case Decode.decodeValue Report.decoder flags.report of
                Ok value ->
                    value

                Err error ->
                    { app = "", components = [], examples = [], crosscuts = [], journeys = [], variables = [] }

        model : Model
        model =
            { theme = Light
            , report = report
            , filters = Filters.empty
            , ui = initUI
            }
    in
    ( model, Cmd.none )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case Debug.log "msg" msg of
        ResultsPageMsg (ToggleExampleExpanded example) ->
            ( { model | ui = toggleExample example model.ui }, Cmd.none )

        ThemeMsg ToggleTheme ->
            ( { model | theme = Theme.toggleTheme model.theme }, Cmd.none )

        OverviewPageMsg (FilterBy filter) ->
            ( { model | filters = Filters.filterBy filter model.filters }, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none


view : Model -> Html Msg
view model =
    Html.div
        [ HA.class "app"
        , Theme.class model.theme
        ]
        [ case List.head model.filters of
            Just (ComponentNameEquals name) ->
                case findComponent name model.report.components of
                    Just component ->
                        viewComponentPage model.report.app component (examplesForComponent name model.report.examples) model.ui.expanded

                    Nothing ->
                        viewComponentNotFoundPage name

            Just Journeys ->
                viewJourneysPage model.report.app model.report.journeys (journeyExamples model.report.examples) model.ui.expanded

            _ ->
                viewOverviewPage model.report.app model.report.components model.report.journeys
        ]


findComponent : String -> List Component -> Maybe Component
findComponent name components =
    List.filter (.component >> (==) name) components |> List.head


type alias UI =
    { expanded : Set String
    }


initUI : UI
initUI =
    { expanded = Set.empty
    }


toggleExample : String -> UI -> UI
toggleExample example ui =
    if Set.member example ui.expanded then
        { ui | expanded = Set.remove example ui.expanded }

    else
        { ui | expanded = Set.insert example ui.expanded }


viewOverviewPage : String -> List Component -> List Journey -> Html Msg
viewOverviewPage app components journeys =
    Html.div
        [ HA.class "overview-page" ]
    <|
        [ OverviewPage.viewHeader app
            |> Html.map ThemeMsg
        , OverviewPage.viewContent components journeys
            |> Html.map OverviewPageMsg
        ]


viewComponentPage : String -> Component -> List Example -> Set String -> Html Msg
viewComponentPage app component examples expanded =
    Html.div
        [ HA.class "component-page" ]
        [ ResultsPage.viewHeader app component.component
            |> Html.map ThemeMsg
        , Html.div [ HA.class "categories" ] (List.map (ResultsPage.viewFeature examples expanded) component.features)
            |> Html.map ResultsPageMsg
        ]


viewJourneysPage : String -> List Journey -> List Example -> Set String -> Html Msg
viewJourneysPage app journeys examples expanded =
    Html.div
        [ HA.class "component-page" ]
        [ ResultsPage.viewHeader app "Journeys"
            |> Html.map ThemeMsg
        , Html.div [ HA.class "categories" ] (List.map (.name >> ResultsPage.viewJourney examples expanded) journeys)
            |> Html.map ResultsPageMsg
        ]
