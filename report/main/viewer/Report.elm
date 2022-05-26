module Report exposing (..)

import Dict exposing (Dict)
import Helpers exposing (debugDecoder)
import Json.Decode as Decode exposing (Decoder)



-- sync report/main/org/aerial/report/Report.kt


type alias Report =
    { app : String
    , components : List Component
    , examples : List Example
    , crosscuts : List Crosscut
    , journeys : List Journey
    , variables : List Variable
    }


type alias Component =
    { component : String
    , desc : String
    , tags : List String
    , features : List String
    , file : String
    , line : Int
    }


type alias Example =
    { category : Category
    , example : String
    , variables : Dict String String
    , tags : List String
    , type_ : ExampleType
    , locations : List Loc
    }


type Category
    = ComponentCategory { component : String, feature : String }
    | JourneyCategory String


type ExampleType
    = Normal
    | HowTo
    | Todo


type alias Loc =
    { file : String
    , line : Int
    }


type alias Crosscut =
    { name : String
    , file : String
    , line : Int
    }


type alias Journey =
    { name : String
    , desc : String
    , file : String
    , line : Int
    }


type alias Variable =
    { name : String
    , values : List String
    , file : String
    , line : Int
    }


decoder : Decoder Report
decoder =
    Decode.map6 Report
        (Decode.field "app" <| Decode.string)
        (Decode.field "components" <| Decode.list componentDecoder)
        (Decode.field "examples" <| Decode.list exampleDecoder)
        (Decode.field "crosscuts" <| Decode.list crosscutDecoder)
        (Decode.field "journeys" <| Decode.list journeyDecoder)
        (Decode.field "variables" <| Decode.list variableDecoder)


componentDecoder : Decoder Component
componentDecoder =
    Decode.map6 Component
        (Decode.field "component" <| Decode.string)
        (Decode.field "desc" <| Decode.string)
        (Decode.field "tags" <| Decode.list Decode.string)
        (Decode.field "features" <| Decode.list Decode.string)
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


exampleDecoder : Decoder Example
exampleDecoder =
    Decode.map6 Example
        (Decode.field "category" <| categoryDecoder)
        (Decode.field "example" <| Decode.string)
        (Decode.field "variables" <| Decode.dict Decode.string)
        (Decode.field "tags" <| Decode.list Decode.string)
        (Decode.field "type" <| exampleTypeDecoder)
        (Decode.field "locations" <| Decode.list locDecoder)


categoryDecoder : Decoder Category
categoryDecoder =
    Decode.oneOf
        [ Decode.map2
            (\component feature -> ComponentCategory { component = component, feature = feature })
            (Decode.field "component" Decode.string)
            (Decode.field "feature" Decode.string)
        , Decode.map JourneyCategory <|
            Decode.field "journey" Decode.string
        ]


exampleTypeDecoder : Decoder ExampleType
exampleTypeDecoder =
    Decode.string
        |> Decode.andThen
            (\val ->
                case val |> String.toUpper of
                    "EXAMPLE" ->
                        Decode.succeed Normal

                    "HOW_TO" ->
                        Decode.succeed HowTo

                    "TODO" ->
                        Decode.succeed Todo

                    _ ->
                        Decode.fail <| "Could not parse example type: " ++ val
            )


crosscutDecoder : Decoder Crosscut
crosscutDecoder =
    Decode.map3 Crosscut
        (Decode.field "name" <| Decode.string)
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


journeyDecoder : Decoder Journey
journeyDecoder =
    Decode.map4 Journey
        (Decode.field "name" <| Decode.string)
        (Decode.field "desc" <| Decode.string)
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


variableDecoder : Decoder Variable
variableDecoder =
    Decode.map4 Variable
        (Decode.field "name" <| Decode.string)
        (Decode.field "values" <| Decode.list Decode.string)
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


locDecoder : Decoder Loc
locDecoder =
    Decode.map2 Loc
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


matchesFeature query category =
    case category of
        ComponentCategory { feature } ->
            feature == query

        _ ->
            False


matchesComponent query category =
    case category of
        ComponentCategory { component } ->
            component == query

        _ ->
            False
