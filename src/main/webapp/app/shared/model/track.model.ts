import { IAlbum } from 'app/shared/model/album.model';

export interface ITrack {
  id?: number;
  name?: string;
  album?: IAlbum;
}

export class Track implements ITrack {
  constructor(public id?: number, public name?: string, public album?: IAlbum) {}
}
