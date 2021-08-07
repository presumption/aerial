import {Elm} from "./Aerial.elm";
import report from "../../../build/report.json";

const app = Elm.Aerial.init({flags: {report: report}});
