export interface MsgResponse<T> {
    success: boolean;
    msg: string;
    code: number;
    time: Date;
    data: T;
}