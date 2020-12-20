interface OrderColumn {
    orderColumn: string;
    asc: boolean;
}

export interface PageParams {
    pageNum: number;
    pageSize: number;
    searchCount?: boolean;
    createTime?: Date | string;
    updateTime?: Date | string;
    orderColumns?: OrderColumn[];
    createTime?: string[] | undefined;
    updateTime?: string[] | undefined;
}

export interface PageResult<T> {
    pageNum: number;
    pageSize: number;
    total: number;
    pages: number;
    list: T[];
}